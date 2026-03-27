import test from "node:test";
import assert from "node:assert/strict";
import { PassThrough } from "node:stream";
import { createRequestHandler } from "../src/createRequestHandler.js";

function createMockRequest({ method, url, body }) {
  const req = new PassThrough();
  req.method = method;
  req.url = url;

  process.nextTick(() => {
    if (body !== undefined) {
      req.end(JSON.stringify(body));
      return;
    }

    req.end();
  });

  return req;
}

class MockResponse {
  constructor() {
    this.statusCode = 200;
    this.headers = {};
    this.body = Buffer.alloc(0);
  }

  writeHead(statusCode, headers) {
    this.statusCode = statusCode;
    this.headers = headers;
  }

  end(chunk) {
    if (chunk !== undefined) {
      this.body = Buffer.isBuffer(chunk) ? chunk : Buffer.from(chunk);
    }
  }
}

async function request({ handler, method, url, body }) {
  const req = createMockRequest({ method, url, body });
  const res = new MockResponse();
  await handler(req, res);
  return res;
}

test("GET /health returns ok", async (t) => {
  const handler = createRequestHandler({
    renderPdf: async () => Buffer.from("pdf"),
  });

  const response = await request({ handler, method: "GET", url: "/health" });

  assert.equal(response.statusCode, 200);
  assert.equal(response.headers["Content-Type"], "application/json");
  assert.deepEqual(JSON.parse(response.body.toString("utf8")), { ok: true });
});

test("POST /render returns 400 when url is missing", async () => {
  const handler = createRequestHandler({
    renderPdf: async () => Buffer.from("pdf"),
  });

  const response = await request({
    handler,
    method: "POST",
    url: "/render",
    body: {},
  });

  assert.equal(response.statusCode, 400);
  assert.deepEqual(JSON.parse(response.body.toString("utf8")), {
    message: "url is required",
  });
});

test("POST /render returns PDF when renderer succeeds", async () => {
  const handler = createRequestHandler({
    renderPdf: async ({ url }) => {
      assert.equal(url, "http://example.test/print");
      return Buffer.from("%PDF-1.7");
    },
  });

  const response = await request({
    handler,
    method: "POST",
    url: "/render",
    body: { url: "http://example.test/print" },
  });

  assert.equal(response.statusCode, 200);
  assert.equal(response.headers["Content-Type"], "application/pdf");
  assert.equal(response.body.toString("utf8"), "%PDF-1.7");
});

test("POST /render returns 500 when renderer fails", async () => {
  const handler = createRequestHandler({
    renderPdf: async () => {
      throw new Error("boom");
    },
  });

  const response = await request({
    handler,
    method: "POST",
    url: "/render",
    body: { url: "http://example.test/print" },
  });

  assert.equal(response.statusCode, 500);
  assert.deepEqual(JSON.parse(response.body.toString("utf8")), {
    message: "failed to render pdf",
    detail: "boom",
  });
});
