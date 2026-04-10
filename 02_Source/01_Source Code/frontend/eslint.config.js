import js from '@eslint/js'
import globals from 'globals'
import reactPlugin from "eslint-plugin-react";
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import prettier from 'eslint-config-prettier'
import { defineConfig, globalIgnores } from 'eslint/config'

export default defineConfig([
  globalIgnores(['dist', 'build', 'node_modules', '.git']),
  {
    files: ['**/*.{js,jsx}'],
    languageOptions: {
      ecmaVersion: 2020,
      sourceType: 'module',
      globals: globals.browser,
      parserOptions: {
        ecmaVersion: 'latest',
        ecmaFeatures: { jsx: true },
        sourceType: 'module',
      },
    },
    extends: [
      js.configs.recommended,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
    ],
    rules: {
      // Error prevention
      'no-console': ['warn', { allow: ['warn', 'error'] }],
      'no-debugger': 'warn',
      'no-unused-vars': ['warn', { argsIgnorePattern: '^_', varsIgnorePattern: '^_' }],
      'no-unassigned-vars': 'error',
      'no-unreachable': 'warn',
      
      // React Hooks
      'react-hooks/rules-of-hooks': 'error',
      'react-hooks/exhaustive-deps': 'warn',
      
      // React Refresh
      'react-refresh/only-export-components': 'warn',
    },
  },
  {
    files: ['**/*.jsx'],
    rules: {
      // Additional rules for JSX files
      'no-undef': 'off', // JSX is defined
    },
  },
  {
    files: ["**/*.{js,jsx,mjs,cjs,ts,tsx}"],
    plugins: {
      react: reactPlugin,
    },
    languageOptions: {
      parserOptions: {
        ecmaFeatures: {
          jsx: true,
        },
      },
      globals: {
        ...globals.browser,
      },
    },
    rules: {
      // These are the core rules for catching JSX usage
      "react/jsx-uses-react": "error",   // Prevents 'React' from being marked as unused
      "react/jsx-uses-vars": "error",    // Prevents variables used in JSX from being marked as unused
      "no-unused-vars": "warn",          // The base rule that now respects JSX usage
      "react/jsx-no-undef": "error",     // Catches if you use a component that isn't imported
    },
    settings: {
      react: {
        version: "detect", // Automatically detects your React version
      },
    },
  },
  // Disable ESLint rules that conflict with Prettier
  prettier,
])
