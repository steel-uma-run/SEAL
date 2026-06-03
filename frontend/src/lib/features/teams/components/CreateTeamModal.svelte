<script lang="ts">
  import { toast } from '$lib/components/toast.svelte';

  let { isOpen, onClose, onCreate } = $props<{
    isOpen: boolean;
    onClose: () => void;
    onCreate: (name: string) => Promise<void>;
  }>();

  let teamName = $state('');
  let isSubmitting = $state(false);

  async function handleSubmit(e: Event) {
    e.preventDefault();
    if (!teamName.trim()) {
      toast.error("Vui lòng nhập tên đội");
      return;
    }

    isSubmitting = true;
    try {
      await onCreate(teamName);
      toast.success("Tạo đội thành công!");
      teamName = '';
      onClose();
    } catch (error: any) {
      toast.error(error.message || "Lỗi khi tạo đội");
    } finally {
      isSubmitting = false;
    }
  }
</script>

{#if isOpen}
  <!-- Backdrop -->
  <div class="fixed inset-0 bg-black/40 z-40 flex items-center justify-center p-4" onclick={onClose} role="dialog" tabindex="-1" onkeydown={(e) => e.key === 'Escape' && onClose()}>
    <!-- Modal -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl w-full max-w-md shadow-lg overflow-hidden" onclick={(e) => e.stopPropagation()} role="document" tabindex="0">
      
      <div class="px-6 py-4 border-b border-gray-100 dark:border-gray-700 flex justify-between items-center">
        <h3 class="text-lg font-bold text-gray-900 dark:text-white">Tạo đội mới</h3>
        <button onclick={onClose} class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 transition-colors">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
        </button>
      </div>

      <form onsubmit={handleSubmit} class="p-6 space-y-4">
        <div>
          <label for="teamName" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Tên đội của bạn</label>
          <input 
            id="teamName" 
            type="text" 
            bind:value={teamName}
            placeholder="Nhập tên đội..." 
            class="block w-full px-4 py-3 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-xl text-gray-900 dark:text-white focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none transition-all"
            required
            disabled={isSubmitting}
          />
        </div>

        <div class="flex justify-end space-x-3 pt-4">
          <button type="button" onclick={onClose} class="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg transition-colors" disabled={isSubmitting}>
            Hủy
          </button>
          <button type="submit" class="px-4 py-2 text-sm font-medium text-white bg-orange-600 hover:bg-orange-700 rounded-lg transition-colors flex items-center" disabled={isSubmitting}>
            {#if isSubmitting}
              <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
            {/if}
            Xác nhận tạo
          </button>
        </div>
      </form>
    </div>
  </div>
{/if}
