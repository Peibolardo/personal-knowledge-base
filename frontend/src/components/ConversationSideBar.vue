<template>
    <v-navigation-drawer permanent width="280">

    <!-- New chat button -->
    <div class="pa-3">
        <v-btn
            block
            color="primary"
            variant="flat"
            rounded="lg"
            prepend-icon="mdi-plus"
            @click="handleNewChat"
        >
        New Chat
        </v-btn>
    </div>

    <v-divider />

    <!-- Conversations list -->
    <v-list density="comfortable" nav>

    <!-- Loading -->
        <div v-if="loading" class="d-flex justify-center pa-4">
            <v-progress-circular indeterminate size="24" width="2" color="primary" />
        </div>

        <!-- Empty state -->
        <div v-else-if="conversations.length === 0" class="pa-4 text-center">
            <p class="text-caption text-medium-emphasis">No conversations yet</p>
        </div>

        <!-- List items -->
            <v-list-item
            v-else
            v-for="conv in conversations"
            :key="conv.id"
            :active="conv.id === activeConversationId"
            :title="conv.title"
            rounded="lg"
            density="comfortable"
            @click="handleSelect(conv.id)"
        >
        <template #prepend>
            <v-icon size="20">mdi-message-text-outline</v-icon>
        </template>
        </v-list-item>

    </v-list>

    </v-navigation-drawer>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useConversation } from '@/composables/useConversation'
import { useConversationStore } from '@/stores/conversationStore'
import { storeToRefs } from 'pinia'
import type Conversation from '@/interfaces/Conversation'

const emit = defineEmits<{
    (e: 'select', conversationId: string): void
    (e: 'new-chat'): void
}>()

const { getAllConversations, selectConversation, resetConversation } = useConversation()
const conversationStore = useConversationStore()
const { activeConversationId } = storeToRefs(conversationStore)

const conversations = ref<Conversation[]>([])
const loading = ref(false)

async function loadConversations() {
    loading.value = true
    try {
    const result = await getAllConversations()
    conversations.value = result ?? []
    } finally {
    loading.value = false
    }
}

function handleSelect(conversationId: string) {
    selectConversation(conversationId)
    emit('select', conversationId)
}

function handleNewChat() {
    resetConversation()
    emit('new-chat')
}

// Expose so parent can refresh the list after sending the first message of a new conversation
defineExpose({ loadConversations })

onMounted(loadConversations)
</script>