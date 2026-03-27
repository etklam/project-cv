import { createServer } from "./createServer.js";
import { renderPdf } from "./renderPdf.js";

const port = Number(process.env.PORT || 3100);
const server = createServer({ renderPdf });

server.listen(port, () => {
  console.log(`pdf-renderer listening on ${port}`);
});
