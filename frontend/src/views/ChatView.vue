<template>
  <v-container class="chat-wrapper d-flex flex-column" max-width="780">

    <!-- Header -->
    <div class="py-4">
      <h1 class="text-h6 font-weight-bold">AI Chat</h1>
      <p class="text-caption text-medium-emphasis ma-0">Powered by OpenAI</p>
    </div>

    <v-divider class="mb-4" />

    <!-- Messages area -->
    <div class="messages-area flex-grow-1" ref="messagesArea">

      <!-- Empty state -->
      <div v-if="messages.length === 0" class="d-flex flex-column align-center justify-center h-100 text-center">
        <v-icon size="56" color="primary" class="mb-3">mdi-robot-outline</v-icon>
        <p class="text-body-1 font-weight-medium">How can I help you today?</p>
        <p class="text-caption text-medium-emphasis">Send a message to start the conversation</p>
      </div>

      <!-- Message bubbles -->
      <template v-else>
        <div v-for="(msg, index) in messages" :key="index" class="mb-5">

          <!-- User message -->
          <div v-if="msg.role === 'user'" class="d-flex justify-end">
            <v-card
              color="primary"
              rounded="xl"
              elevation="0"
              class="px-4 py-2 message-bubble"
              max-width="72%"
            >
              <p class="text-body-2 ma-0" style="color: white; white-space: pre-wrap">{{ msg.content }}</p>
            </v-card>
          </div>

          <!-- AI message -->
          <div v-else class="d-flex justify-start align-start ga-2">
            <v-avatar color="surface-variant" size="32" class="mt-1">
              <v-icon size="18">mdi-robot-outline</v-icon>
            </v-avatar>
            <v-card
              rounded="xl"
              elevation="0"
              variant="tonal"
              class="px-4 py-2 message-bubble"
              max-width="72%"
            >
              <p class="text-body-2 ma-0" style="white-space: pre-wrap">{{ msg.content }}</p>
            </v-card>
          </div>

        </div>

        <!-- Loading bubble -->
        <div v-if="loading" class="d-flex justify-start align-start ga-2 mb-5">
          <v-avatar color="surface-variant" size="32" class="mt-1">
            <v-icon size="18">mdi-robot-outline</v-icon>
          </v-avatar>
          <v-card rounded="xl" elevation="0" variant="tonal" class="px-4 py-3">
            <div class="d-flex ga-1 align-center">
              <span class="dot" />
              <span class="dot dot--delay1" />
              <span class="dot dot--delay2" />
            </div>
          </v-card>
        </div>
      </template>

    </div>

    <!-- Error -->
    <v-alert
      v-if="error"
      type="error"
      variant="tonal"
      closable
      class="mb-3"
      density="compact"
      @click:close="error = null"
    >
      {{ error }}
    </v-alert>

    <v-divider class="mb-3" />

    <!-- Input -->
    <div class="input-area">
      <v-textarea
        v-model="inputText"
        placeholder="Type a message..."
        variant="outlined"
        rounded="xl"
        rows="1"
        auto-grow
        max-rows="5"
        hide-details
        density="comfortable"
        :disabled="loading"
        @keydown.enter.exact.prevent="submit"
      >
        <template #append-inner>
          <v-btn
            :icon="loading ? undefined : 'mdi-send'"
            color="primary"
            variant="flat"
            size="small"
            rounded="lg"
            :disabled="!inputText.trim() || loading"
            :loading="loading"
            @click="submit"
          />
        </template>
      </v-textarea>
      <p class="text-caption text-medium-emphasis mt-1 ml-1">
        Enter to send · Shift+Enter for new line
      </p>
    </div>

  </v-container>
</template>

<script lang="ts" setup>
import { ref, nextTick } from 'vue'
import { useAiApi } from '@/composables/useAiApi'

interface Message {
  role: 'user' | 'ai'
  content: string
}

const { sendMessageToApi } = useAiApi()

const messages = ref<Message[]>([])
const inputText = ref('')
const loading = ref(false)
const error = ref<string | null>(null)
const messagesArea = ref<HTMLElement | null>(null)

function scrollToBottom() {
  nextTick(() => {
    if (messagesArea.value) {
      messagesArea.value.scrollTop = messagesArea.value.scrollHeight
    }
  })
}

async function submit() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value = true
  error.value = null
  scrollToBottom()

  try {
    const response = await sendMessageToApi(text)
    messages.value.push({
      role: 'ai',
      content: response ?? 'No response received'
    })
  } catch {
    error.value = 'Something went wrong. Make sure the backend services are running.'
  } finally {
    loading.value = false
    scrollToBottom()
  }
}
</script>

<style scoped>
.chat-wrapper {
  height: 100vh;
  padding-bottom: 16px;
}

.messages-area {
  overflow-y: auto;
  padding-right: 4px;
  min-height: 0;
}

.message-bubble {
  word-break: break-word;
}

.dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background-color: currentColor;
  opacity: 0.4;
  animation: bounce 1.2s infinite ease-in-out;
}
.dot--delay1 { animation-delay: 0.2s; }
.dot--delay2 { animation-delay: 0.4s; }

@keyframes bounce {
  0%, 80%, 100% { transform: translateY(0); opacity: 0.4; }
  40%            { transform: translateY(-5px); opacity: 1; }
}
</style>