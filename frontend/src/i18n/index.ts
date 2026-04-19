import i18n from 'i18next';
import LanguageDetector from 'i18next-browser-languagedetector';
import { initReactI18next } from 'react-i18next';

import en from './locales/en.json';
import et from './locales/et.json';
import ru from './locales/ru.json';

i18n
  .use(LanguageDetector)   // reads language from localStorage, saves it there too
  .use(initReactI18next)   // connects i18next to React's hook system
  .init({
    resources: {
      en: { translation: en },
      et: { translation: et },
      ru: { translation: ru },
    },
    fallbackLng: 'en',           // if a key is missing in et/ru, fall back to English
    interpolation: {
      escapeValue: false,        // React already protects against XSS, no need to double-escape
    },
    detection: {
      order: ['localStorage', 'navigator'],  // check localStorage first, then browser language
      caches: ['localStorage'],              // save the user's choice to localStorage
    },
  });

export default i18n;
