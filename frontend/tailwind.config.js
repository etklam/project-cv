import forms from "@tailwindcss/forms";

/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          ink: "#1A1B22",
          muted: "#505F76",
          accent: "#003AA0",
          accentSoft: "#D3E4FE",
          paper: "#FBF8FF",
          panel: "#F4F2FC",
          warm: "#F59E0B",
        },
      },
      fontFamily: {
        sans: ["Inter", "sans-serif"],
        display: ["Manrope", "sans-serif"],
        mono: ["Fira Code", "monospace"],
      },
      boxShadow: {
        soft: "0 24px 48px rgba(26, 27, 34, 0.06)",
      },
    },
  },
  plugins: [forms],
};
