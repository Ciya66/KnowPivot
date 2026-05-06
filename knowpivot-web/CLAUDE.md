# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

KnowPivot is an enterprise AI knowledge base platform (企业级 AI 知识库平台) — an Agentic intelligent workspace with multi-turn dialogue, knowledge management, document understanding, and AI-powered Q&A. The UI language is Chinese (`zh-CN`). Desktop-first design, no responsive breakpoints.

## Commands

```bash
npm run dev          # Start Vite dev server (default: localhost:5173)
npm run build        # Type-check + production build (parallel)
npm run build-only   # Production build without type-check
npm run type-check   # vue-tsc --build (TypeScript validation)
npm run preview      # Preview production build locally
```

No linter, formatter, or test framework is configured.

## Architecture

### Layout System

Three-column layout managed by `DefaultLayout.vue`:

```
┌──────────┬──────────────────────────┬────────────┐
│ SideNav  │  Main Content (slot)     │ RightPanel │
│  240px   │  flex: 1                 │  320px     │
│ collapsible                         │ conditional│
└──────────┴──────────────────────────┴────────────┘
```

- `App.vue` wraps everything in `DefaultLayout`, passing `togglePanel` via scoped slot
- `RightPanel` (reasoning chain + citations) is toggled by DashboardView and animated via `<Transition name="slide-left">`
- Page transitions use `<Transition name="fade-slide" mode="out-in">` around `<router-view>`

### Routing

`vue-router` v5 with HTML5 history mode. All route components are lazy-loaded via `() => import(...)`:

| Path | Name | View |
|------|------|------|
| `/` | `dashboard` | DashboardView |
| `/knowledge` | `knowledge` | KnowledgeView |
| `/agent` | `agent` | AgentView |
| `/documents` | `documents` | DocumentsView |
| `/settings` | `settings` | SettingsView |

### State Management

Pinia v3 with Composition API style (`defineStore` with setup function). Store at `stores/counter.ts` (misnamed from scaffold; store ID is `app`). Manages `theme` and `sidebarCollapsed`.

### Styling

Pure CSS with custom properties — no preprocessor, no Tailwind, no component library.

- **Design tokens**: `src/styles/variables.css` — all colors, spacing (8pt grid), radii, shadows, typography, layout dimensions, z-index scale
- **Global reset + utilities**: `src/styles/global.css` — includes Google Fonts (Inter + JetBrains Mono)
- **Transition presets**: `src/styles/transitions.css` — fade, fade-slide, scale-fade, slide-left, typing-cursor, pulse, skeleton
- **All component styles are `<style scoped>`** and reference CSS variables from `variables.css`
- **Dark theme**: activated by `[data-theme='dark']` on `<html>`. Override tokens inline in each component's scoped styles with `[data-theme='dark']` selectors

### Design Language

Modern Minimalism + AI Tech + Enterprise-grade (Notion + Linear + ChatGPT fusion). See `.claude/skills/ui-design-style/SKILL.md` for the full specification including colors, fonts, component requirements, and animation guidelines.

Key constraints:
- Primary blue `#3B82F6` / `#2563EB`; Accent purple `#8B5CF6` (AI/Agent features)
- Border radius: 8px / 12px; 1px light borders; light shadows
- Icons: inline SVGs only (no icon library)
- Animations: functional, not decorative — typewriter cursor, agent thinking pulse, card hover lift, skeleton shimmer

## Conventions

- **SFC pattern**: `<script setup lang="ts">` with `<template>` and `<style scoped>`
- **Path alias**: `@/` maps to `src/` (configured in both vite.config.ts and tsconfig.app.json)
- **TypeScript**: `noUncheckedIndexedAccess: true` is enabled — array/object lookups return `T | undefined`
- **Store pattern**: Composition API with `defineStore('name', () => { ... return { ... } })`
- **File size limit**: 300 lines max per SFC (enforced by `.claude/agents/ui-dev.md`)
- **Dark mode support required**: every new component must include `[data-theme='dark']` overrides

## Current State

All data is static/mock. No API layer, no authentication, no environment files, no i18n framework (UI strings are hardcoded Chinese). The `ui-dev` agent in `.claude/agents/` is available for Vue3 component development following the design spec.
