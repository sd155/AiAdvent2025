# Chat Module

This module provides multiplatform chat functionality with AI integration, used throughout the [AI Advent 2025 application](../../README.md).

## Features
- **AI Chat Interface**: Integration with OpenRouter API using Qwen 3.5B model
- **Real-time Messaging**: Live typing indicators and error handling

---

## AI Integration

### OpenRouter API
- **Endpoint**: `https://openrouter.ai/api/v1/chat/completions`
- **Model**: `qwen/qwen3-235b-a22b:free`
- **Authentication**: Bearer token via API key
- **Context Management**: Maintains conversation history for context-aware responses

