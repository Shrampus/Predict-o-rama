/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}', './src/**/*.css'],
  theme: {
    extend: {
      screens: {
        phone: '480px',
        // Tailwind default breakpoints are still available: sm/md/lg/xl/2xl.
        tablet: '768px',
        laptop: '1024px',
        desktop: '1280px',
        widescreen: '1440px',
      },
    },
  },
  plugins: [],
}

