import type ChatStreamContent from './streaming/ChatStreamContent'
import type ChatStreamDone from './streaming/ChatStreamDone'
import type ChatStreamMetadata from './streaming/ChatStreamMetadata'
import type ChatStreamError from './streaming/ChatStreamError'

export type StreamEventType = 
| { type: 'content'; data: ChatStreamContent }
| { type: 'done'; data: ChatStreamDone }
| { type: 'metadata'; data: ChatStreamMetadata }
| { type: 'error'; data: ChatStreamError }