import http from "node:http";
import { createRequestHandler } from "./createRequestHandler.js";

export function createServer({ renderPdf }) {
  return http.createServer(createRequestHandler({ renderPdf }));
}
