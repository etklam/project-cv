function readJson(req) {
  return new Promise((resolve, reject) => {
    let body = "";

    req.on("data", (chunk) => {
      body += chunk;
    });

    req.on("end", () => {
      try {
        resolve(body ? JSON.parse(body) : {});
      } catch (error) {
        reject(error);
      }
    });

    req.on("error", reject);
  });
}

export function createRequestHandler({ renderPdf }) {
  return async (req, res) => {
    if (req.method === "GET" && req.url === "/health") {
      res.writeHead(200, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ ok: true }));
      return;
    }

    if (req.method === "POST" && req.url === "/render") {
      try {
        const payload = await readJson(req);

        if (!payload.url) {
          res.writeHead(400, { "Content-Type": "application/json" });
          res.end(JSON.stringify({ message: "url is required" }));
          return;
        }

        const pdfBuffer = await renderPdf({ url: payload.url });
        res.writeHead(200, {
          "Content-Type": "application/pdf",
          "Content-Length": pdfBuffer.length,
        });
        res.end(pdfBuffer);
        return;
      } catch (error) {
        res.writeHead(500, { "Content-Type": "application/json" });
        res.end(
          JSON.stringify({
            message: "failed to render pdf",
            detail: error instanceof Error ? error.message : String(error),
          }),
        );
        return;
      }
    }

    res.writeHead(404, { "Content-Type": "application/json" });
    res.end(JSON.stringify({ message: "not found" }));
  };
}
