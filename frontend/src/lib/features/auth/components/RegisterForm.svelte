<script lang="ts">
  import { register } from '../api';
  
  let email = $state('');
  let password = $state('');
  let confirmPassword = $state('');
  let fullName = $state('');
  let studentCode = $state('');
  let studentType = $state<'FPT' | 'EXTERNAL' | ''>('');
  
  let { onToggleMode } = $props<{ onToggleMode: () => void }>();

  async function handleSubmit(event: Event) {
    event.preventDefault();
    
    if (password !== confirmPassword) {
      alert("Mật khẩu xác nhận không khớp!");
      return;
    }

    const params = new URLSearchParams();
    params.append('email', email);
    params.append('name', fullName);
    params.append('password', password);
    params.append('external', String(studentType === 'EXTERNAL')); 

    try {
      await register(params);
      alert("Đăng ký thành công! Vui lòng đăng nhập.");
      onToggleMode();
    } catch (error: any) {
      alert("Lỗi đăng ký: " + error.message);
    }
  }
</script>

<div class="flex flex-col items-center justify-center">
  <div class="w-16 h-16 rounded-full border-2 border-purple-500/30 dark:border-purple-500/50 flex items-center justify-center mb-6 shadow-[0_0_20px_rgba(168,85,247,0.3)] dark:shadow-[0_0_20px_rgba(168,85,247,0.5)] bg-white/50 dark:bg-black/30 backdrop-blur-sm">
    <span class="text-2xl font-bold bg-gradient-to-br from-purple-500 to-blue-600 dark:from-purple-400 dark:to-blue-500 bg-clip-text text-transparent">SE</span>
  </div>
  <h2 class="mt-2 text-center text-3xl font-extrabold text-gray-900 dark:text-white tracking-tight">Tạo tài khoản</h2>
  <p class="mt-2 text-sm text-gray-500 dark:text-gray-400">Tham gia cộng đồng của chúng tôi</p>
</div>

<form class="mt-8 space-y-5" onsubmit={handleSubmit}>
  <div class="space-y-4">
    <div>
      <label for="reg-email" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Email</label>
      <input id="reg-email" type="email" required bind:value={email} class="block w-full px-4 py-3 bg-gray-50 dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-xl text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all outline-none" placeholder="you@example.com" />
    </div>

    <div>
      <label for="reg-password" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Mật khẩu</label>
      <input id="reg-password" type="password" required bind:value={password} class="block w-full px-4 py-3 bg-gray-50 dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-xl text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all outline-none" placeholder="••••••••" />
    </div>

    <div class="animate-in fade-in slide-in-from-top-4 duration-300 space-y-4">
      <div>
        <label for="confirmPassword" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Xác nhận mật khẩu</label>
        <input id="confirmPassword" type="password" required bind:value={confirmPassword} class="block w-full px-4 py-3 bg-gray-50 dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-xl text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all outline-none" placeholder="••••••••" />
      </div>

      <div>
        <label for="fullName" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Họ và tên</label>
        <input id="fullName" type="text" required bind:value={fullName} class="block w-full px-4 py-3 bg-gray-50 dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-xl text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all outline-none" placeholder="Nguyễn Văn A" />
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <label for="studentCode" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Mã SV</label>
          <input id="studentCode" type="text" required bind:value={studentCode} placeholder="SE203..." class="block w-full px-4 py-3 bg-gray-50 dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-xl text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all outline-none" />
        </div>

        <div>
          <label for="studentType" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Trường học</label>
          <select id="studentType" required bind:value={studentType} class="block w-full px-4 py-3 bg-gray-50 dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-xl text-gray-900 dark:text-white focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all outline-none appearance-none [&>option]:bg-white dark:[&>option]:bg-gray-900">
            <option value="" disabled>-- Chọn --</option>
            <option value="FPT">FPTU</option>
            <option value="EXTERNAL">External</option>
          </select>
        </div>
      </div>
    </div>
  </div>

  <div class="pt-2">
    <button type="submit" class="w-full flex justify-center py-3.5 px-4 border border-transparent text-sm font-bold rounded-xl text-white bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-500 hover:to-blue-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-gray-50 dark:focus:ring-offset-gray-900 focus:ring-purple-500 shadow-md dark:shadow-[0_0_15px_rgba(168,85,247,0.4)] transition-all transform hover:scale-[1.02]">
      Đăng ký
    </button>
  </div>
</form>

<div class="text-center mt-6">
  <p class="text-gray-500 dark:text-gray-400 text-sm">
    Đã có tài khoản? 
    <button type="button" onclick={onToggleMode} class="font-bold text-purple-600 dark:text-transparent dark:bg-gradient-to-r dark:from-purple-400 dark:to-pink-500 dark:bg-clip-text hover:brightness-125 transition-all focus:outline-none ml-1 cursor-pointer">
      Đăng nhập
    </button>
  </p>
</div>
