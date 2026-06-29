<script lang="ts">
    import { notificationsStore, type NotificationItem } from "$lib/notifications.svelte"
    import { theme } from "$lib/theme.svelte"
    import { goto } from "$app/navigation"
    import { 
        Bell, 
        Check, 
        CheckCheck, 
        Trash2, 
        Info, 
        CheckCircle, 
        AlertTriangle, 
        AlertCircle
    } from "@lucide/svelte"

    let isOpen = $state(false)
    let container = $state<HTMLDivElement | null>(null)

    // Count unread notifications
    let unreadCount = $derived(notificationsStore.list.filter((n: NotificationItem) => n.unread).length)

    function toggleDropdown() {
        isOpen = !isOpen
    }

    function handleWindowClick(e: MouseEvent) {
        if (isOpen && container && !container.contains(e.target as Node)) {
            isOpen = false
        }
    }

    function handleMarkAsRead(id: string, e: Event) {
        e.stopPropagation()
        notificationsStore.markAsRead(id)
    }

    function handleMarkAllAsRead() {
        notificationsStore.markAllAsRead()
    }

    function handleClearAll() {
        notificationsStore.clearAll()
    }

    async function handleNotificationClick(item: NotificationItem) {
        notificationsStore.markAsRead(item.id)
        isOpen = false
        if (item.link) {
            await goto(item.link)
        }
    }

    // Format relative time (e.g. "5m ago")
    function formatRelativeTime(dateStr: string): string {
        const date = new Date(dateStr)
        const now = new Date()
        const diffMs = now.getTime() - date.getTime()
        
        if (diffMs < 0 || diffMs < 30000) return "Just now"
        
        const diffMins = Math.floor(diffMs / 60000)
        if (diffMins < 60) return `${diffMins}m ago`
        
        const diffHours = Math.floor(diffMins / 60)
        if (diffHours < 24) return `${diffHours}h ago`
        
        const diffDays = Math.floor(diffHours / 24)
        if (diffDays === 1) return "Yesterday"
        return `${diffDays}d ago`
    }
</script>

<svelte:window onclick={handleWindowClick} />

<div class="relative inline-block text-left" bind:this={container}>
    <button
        class="relative p-2.5 rounded-full transition-all duration-200 hover:cursor-pointer flex items-center justify-center border shadow-sm group {theme.darkMode ? 'bg-zinc-800 border-zinc-700 text-zinc-300 hover:bg-zinc-700 hover:text-white' : 'bg-white border-gray-200 text-gray-600 hover:bg-gray-50 hover:text-gray-900'}"
        onclick={toggleDropdown}
        title="Notifications"
    >
        <Bell class="w-5 h-5 transition-transform duration-300 group-hover:rotate-12" />
        
        {#if unreadCount > 0}
            <span class="absolute -top-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-[#ea580c] text-[0.7rem] font-bold text-white shadow-md animate-pulse">
                {unreadCount > 9 ? '9+' : unreadCount}
            </span>
        {/if}
    </button>

    {#if isOpen}
        <div
            class="absolute right-0 mt-3.5 w-80 md:w-96 rounded-2xl shadow-xl border overflow-hidden z-[2000] origin-top-right transition-all duration-200 ease-out {theme.darkMode 
                ? 'bg-zinc-900 border-zinc-800 shadow-[0_10px_30px_rgba(0,0,0,0.5)]' 
                : 'bg-white border-gray-100 shadow-[0_10px_30px_rgba(0,0,0,0.08)]'}"
        >
            <div class="p-4 flex items-center justify-between border-b {theme.darkMode ? 'border-zinc-800' : 'border-gray-100'}">
                <div class="flex items-center gap-2.5">
                    <h3 class="font-bold text-base tracking-tight {theme.darkMode ? 'text-zinc-100' : 'text-gray-900'}">Notifications</h3>
                    {#if unreadCount > 0}
                        <span class="px-2 py-0.5 text-[10px] font-bold uppercase tracking-wider rounded-full bg-orange-100 dark:bg-orange-500/20 text-[#ea580c] dark:text-orange-400">
                            {unreadCount} new
                        </span>
                    {/if}
                </div>
                {#if notificationsStore.list.length > 0}
                    <button
                        onclick={handleMarkAllAsRead}
                        class="text-xs font-semibold flex items-center gap-1 cursor-pointer transition-colors {theme.darkMode ? 'text-orange-400 hover:text-orange-300' : 'text-[#ea580c] hover:text-[#c2410c]'}"
                        disabled={unreadCount === 0}
                    >
                        <CheckCheck class="w-4 h-4" />
                        Mark all read
                    </button>
                {/if}
            </div>

            <div class="max-h-[360px] overflow-y-auto custom-scrollbar divide-y {theme.darkMode ? 'divide-zinc-800/50' : 'divide-gray-50'}">
                {#if notificationsStore.list.length === 0}
                    <div class="py-12 px-4 flex flex-col items-center justify-center text-center">
                        <div class="p-4 rounded-full mb-4 {theme.darkMode ? 'bg-zinc-800/60 text-zinc-600' : 'bg-gray-50 text-gray-300'}">
                            <Bell class="w-8 h-8" />
                        </div>
                        <p class="font-semibold text-sm {theme.darkMode ? 'text-zinc-300' : 'text-gray-600'}">No notifications yet</p>
                        <p class="text-xs mt-1 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">We'll alert you when there are updates.</p>
                    </div>
                {:else}
                    {#each notificationsStore.list as item (item.id)}
                        <div
                            onclick={() => handleNotificationClick(item)}
                            class="p-4 flex gap-3.5 cursor-pointer transition-all duration-200 select-none group relative {
                                item.unread 
                                    ? (theme.darkMode ? 'bg-zinc-800/30 hover:bg-zinc-800/50' : 'bg-orange-50/30 hover:bg-orange-50/60') 
                                    : (theme.darkMode ? 'hover:bg-zinc-800/40' : 'hover:bg-gray-50')
                            }"
                            role="button"
                            tabindex="0"
                            onkeydown={(e) => e.key === 'Enter' && handleNotificationClick(item)}
                        >
                            <div class="shrink-0 mt-0.5">
                                {#if item.type === 'success'}
                                    <div class="p-2 rounded-xl bg-emerald-100 dark:bg-emerald-500/20 text-emerald-600 dark:text-emerald-400">
                                        <CheckCircle class="w-4.5 h-4.5" />
                                    </div>
                                {:else if item.type === 'warning'}
                                    <div class="p-2 rounded-xl bg-amber-100 dark:bg-amber-500/20 text-amber-600 dark:text-amber-400">
                                        <AlertTriangle class="w-4.5 h-4.5" />
                                    </div>
                                {:else if item.type === 'error'}
                                    <div class="p-2 rounded-xl bg-red-100 dark:bg-red-500/20 text-red-600 dark:text-red-400">
                                        <AlertCircle class="w-4.5 h-4.5" />
                                    </div>
                                {:else}
                                    <div class="p-2 rounded-xl bg-blue-100 dark:bg-blue-500/20 text-blue-600 dark:text-blue-400">
                                        <Info class="w-4.5 h-4.5" />
                                    </div>
                                {/if}
                            </div>

                            <div class="flex-1 min-w-0 pr-6">
                                <div class="flex items-center gap-2">
                                    <p class="text-sm font-bold truncate {
                                        item.unread 
                                            ? (theme.darkMode ? 'text-zinc-100' : 'text-gray-900') 
                                            : (theme.darkMode ? 'text-zinc-400' : 'text-gray-600')
                                    }">
                                        {item.title}
                                    </p>
                                    {#if item.unread}
                                        <span class="w-2 h-2 rounded-full bg-[#ea580c] shrink-0"></span>
                                    {/if}
                                </div>
                                
                                <p class="text-xs mt-1 leading-snug line-clamp-2 {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
                                    {item.description}
                                </p>
                                
                                <p class="text-[11px] mt-2 font-medium {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">
                                    {formatRelativeTime(item.time)}
                                </p>
                            </div>

                            {#if item.unread}
                                <button
                                    onclick={(e) => handleMarkAsRead(item.id, e)}
                                    class="absolute right-3 top-4 p-1.5 rounded-lg opacity-0 group-hover:opacity-100 hover:bg-white dark:hover:bg-zinc-700 text-gray-400 hover:text-gray-700 dark:text-zinc-400 dark:hover:text-zinc-200 shadow-sm border border-transparent hover:border-gray-200 dark:hover:border-zinc-600 transition-all cursor-pointer"
                                    title="Mark as read"
                                >
                                    <Check class="w-3.5 h-3.5" />
                                </button>
                            {/if}
                        </div>
                    {/each}
                {/if}
            </div>

            {#if notificationsStore.list.length > 0}
                <div class="p-3 border-t flex justify-center {theme.darkMode ? 'border-zinc-800 bg-zinc-900/50' : 'border-gray-100 bg-gray-50/50'}">
                    <button
                        onclick={handleClearAll}
                        class="text-xs font-semibold text-gray-500 hover:text-red-600 dark:text-zinc-400 dark:hover:text-red-400 flex items-center gap-1.5 cursor-pointer transition-colors"
                    >
                        <Trash2 class="w-3.5 h-3.5" />
                        Clear all notifications
                    </button>
                </div>
            {/if}
        </div>
    {/if}
</div>

<style>
    /* Custom slim scrollbar */
    .custom-scrollbar::-webkit-scrollbar {
        width: 4px;
    }
    .custom-scrollbar::-webkit-scrollbar-track {
        background: transparent;
    }
    .custom-scrollbar::-webkit-scrollbar-thumb {
        background: rgba(156, 163, 175, 0.3);
        border-radius: 10px;
    }
    .custom-scrollbar::-webkit-scrollbar-thumb:hover {
        background: rgba(156, 163, 175, 0.6);
    }
</style>