<script lang="ts">
    import { onMount } from 'svelte';
    import { getSeasons } from '$lib/api/seasons';

    let activeSeasons: any[] = $state([]);
    let selectedSeason: any = $state(null); 
    let isLoading: boolean = $state(true);

    onMount(async () => {
        try {
            const response = await getSeasons();
            if (response.ok) {
                const data = await response.json();
                if (data && data.length > 0) {
                    activeSeasons = data; 
                    selectedSeason = data[0]; // Mặc định chọn season đầu tiên
                }
            }
        } catch (error) {
            console.error("Lỗi khi tải dữ liệu Season:", error);
        } finally {
            isLoading = false; 
        }
    });

    // Format ngày cho phần Master (VD: 03 JUN)
    function formatDate(dateString: string) {
        if (!dateString) return { day: '--', month: '---' };
        const date = new Date(dateString);
        return {
            day: String(date.getDate()).padStart(2, '0'),
            month: date.toLocaleDateString('en-GB', { month: 'short' }).toUpperCase()
        };
    }

    // Format ngày cho phần Detail (VD: 1st June 2026)
    function formatFullDate(dateString: string) {
        if (!dateString) return 'TBA';
        const date = new Date(dateString);
        const day = date.getDate();
        
        let suffix = 'th';
        if (day % 10 === 1 && day !== 11) suffix = 'st';
        else if (day % 10 === 2 && day !== 12) suffix = 'nd';
        else if (day % 10 === 3 && day !== 13) suffix = 'rd';
        
        const month = date.toLocaleDateString('en-GB', { month: 'short' });
        const year = date.getFullYear();
        
        return `${day}${suffix} ${month} ${year}`;
    }
</script>

<div class="startpage-content">
    
    <section class="relative min-h-[92vh] flex items-center justify-center text-center py-16 px-4 bg-white after:absolute after:bottom-0 after:left-0 after:w-full after:h-[400px] after:bg-gradient-to-b after:from-[rgba(253,250,249,0)] after:to-[#fdfaf9] after:z-[1] after:pointer-events-none">
        <div class="absolute inset-0 z-0">
            <img 
                src="https://img.freepik.com/free-vector/futuristic-white-technology-background_23-2148390336.jpg" 
                alt="Background pattern" 
                class="w-full h-full object-cover opacity-70 transition-all duration-300 dark:invert dark:hue-rotate-180 dark:opacity-20" 
            />
        </div>
        
        <div class="relative z-[2] max-w-[700px]">
            <div class="inline-block bg-[#fff0e6] text-[#f54901] py-[0.6rem] px-[1.2rem] rounded-full text-base font-semibold mb-6">
                Inspiring Innovation, Shaping the Future
            </div>
            <h1 class="text-[2.5rem] md:text-[3.5rem] font-black mb-4 text-[var(--md-on-surface)]">
                <span class="text-[#f54901]">SEAL</span> HACKATHON
            </h1>
            <p class="text-[1.3rem] text-[var(--md-on-surface)] opacity-80 mb-8 leading-relaxed">
                The official event management and student support platform for FPT University. Register now to form your team, submit your project, and experience the platform!
            </p>
            <div class="flex flex-col md:flex-row gap-8 justify-center">
                <a href="/auth/register" class="px-9 py-5 font-semibold text-[1.3rem] rounded-xl cursor-pointer transition-all duration-200 border border-transparent inline-flex items-center justify-center hover:scale-110 bg-[#f54901] text-white shadow-[0_4px_6px_rgba(245,73,1,0.2)] hover:bg-[#d64000]">
                    Join Now
                </a>
                <a href="/auth/login" class="px-9 py-5 font-semibold text-[1.3rem] rounded-xl cursor-pointer transition-all duration-200 border border-[#f54901] inline-flex items-center justify-center hover:scale-110 bg-transparent text-[#f54901] hover:bg-[#f54901]/5">
                    For Registered Teams
                </a>
            </div>
        </div>
    </section>

    <section class="py-16 px-[5%] bg-[#fdfaf9] flex justify-center relative z-[5]">
        <div class="max-w-[1250px] w-full">
            
            <div class="flex flex-col md:flex-row md:items-end justify-between gap-4 mb-10">
                <div>
                    <div class="flex items-center gap-3 mb-2">
                        <div class="w-10 h-10 rounded-full bg-[#fff0e6] flex items-center justify-center shrink-0">
                            <svg class="w-5 h-5 text-[#f54901]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                            </svg>
                        </div>
                        <p class="text-sm font-bold text-[#f54901] uppercase tracking-wider">Happening Now</p>
                    </div>
                    <h2 class="text-3xl md:text-4xl font-extrabold text-gray-900">Current Seasons</h2>
                </div>
            </div>

            {#if isLoading}
                <div class="animate-pulse flex flex-col gap-10">
                    <div class="bg-white rounded-3xl p-6 md:p-10 flex flex-col lg:flex-row gap-10 border border-gray-100 shadow-sm">
                        <div class="w-full lg:w-5/12 h-64 bg-gray-200 rounded-2xl"></div>
                        <div class="w-full lg:w-7/12 flex flex-col justify-center gap-4">
                            <div class="h-10 bg-gray-200 rounded w-3/4"></div>
                            <div class="h-4 bg-gray-200 rounded w-full"></div>
                            <div class="h-4 bg-gray-200 rounded w-5/6"></div>
                        </div>
                    </div>
                    <div class="flex gap-6 overflow-hidden">
                        <div class="w-[300px] h-32 bg-gray-200 rounded-2xl shrink-0"></div>
                        <div class="w-[300px] h-32 bg-gray-200 rounded-2xl shrink-0"></div>
                        <div class="w-[300px] h-32 bg-gray-200 rounded-2xl shrink-0"></div>
                    </div>
                </div>

            {:else if activeSeasons.length === 0}
                <div class="bg-white rounded-3xl border border-gray-200 p-16 text-center shadow-sm">
                    <svg class="w-16 h-16 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                    <h3 class="text-xl font-bold text-gray-700 mb-2">No Active Hackathon</h3>
                    <p class="text-gray-500">There are no seasons running at the moment. Stay tuned!</p>
                </div>

            {:else if selectedSeason}
                <div class="flex flex-col gap-8">
                    
                    <div class="bg-white rounded-3xl shadow-[0_8px_30px_rgba(0,0,0,0.04)] border border-gray-100 p-6 md:p-8 flex flex-col lg:flex-row gap-10 hover:shadow-[0_8px_30px_rgba(245,73,1,0.06)] transition-all duration-300">
                        
                        <div class="w-full lg:w-5/12">
                            <div class="relative w-full aspect-[4/3] rounded-2xl overflow-hidden bg-[#fff0e6]">
                                <img 
                                    src={selectedSeason.imageUrl || ""} 
                                    alt={selectedSeason.name}
                                    class="w-full h-full object-cover"
                                />
                                <div class="absolute top-4 left-4 bg-white/90 backdrop-blur px-3 py-1.5 rounded-lg text-sm font-bold text-[#f54901] shadow-sm">
                                    LIVE
                                </div>
                            </div>
                        </div>

                        <div class="w-full lg:w-7/12 flex flex-col">
                            <h3 class="text-3xl md:text-4xl font-extrabold text-gray-900 mb-10 leading-tight">
                                {selectedSeason.name}
                            </h3>
                            
                            <p class="text-lg text-gray-600 leading-relaxed mb-8 flex-1">
                                {selectedSeason.description || 'Join our latest hackathon season to challenge yourself, meet like-minded peers, and build innovative solutions. Mentorship and resources will be provided throughout the journey.'}
                            </p>

                            <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-6 pt-6 border-t border-gray-100">
                                
                                <div class="flex items-center gap-4">
                                    <div class="flex flex-col items-center justify-center border border-gray-200 rounded-xl min-w-[75px] py-2 bg-gray-50">
                                        <span class="text-[11px] text-gray-500 uppercase font-bold tracking-wider">Starts</span>
                                        <div class="flex flex-col items-center leading-none mt-1">
                                            <span class="text-2xl font-black text-gray-800">{formatDate(selectedSeason.start_time).day}</span>
                                            <span class="text-xs font-bold text-gray-500 mt-1">{formatDate(selectedSeason.start_time).month}</span>
                                        </div>
                                    </div>

                                    <div class="w-6 h-[2px] bg-gray-200 rounded-full"></div>

                                    <div class="flex flex-col items-center justify-center border border-[#fcece3] rounded-xl min-w-[75px] py-2 bg-[#fff0e6]">
                                        <span class="text-[11px] text-[#f54901] uppercase font-bold tracking-wider">Ends</span>
                                        <div class="flex flex-col items-center leading-none mt-1">
                                            <span class="text-2xl font-black text-[#f54901]">{formatDate(selectedSeason.end_time).day}</span>
                                            <span class="text-xs font-bold text-[#f54901] mt-1">{formatDate(selectedSeason.end_time).month}</span>
                                        </div>
                                    </div>
                                </div>

                                <a href="/dashboard/seasons/{selectedSeason.id || selectedSeason._id || ''}" class="px-8 py-3.5 rounded-xl bg-[#f54901] text-white font-bold text-center tracking-wide hover:bg-[#d64000] hover:scale-105 hover:shadow-lg transition-all duration-300 flex items-center justify-center gap-2 w-full sm:w-auto">
                                    View Details 
                                    <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" /></svg>
                                </a>
                            </div>
                        </div>
                    </div>

                    <div class="flex gap-4 overflow-x-auto pb-6 pt-2 px-1 custom-scrollbar">
                        {#each activeSeasons as season}
                            <button 
                                onclick={() => selectedSeason = season}
                                class="min-w-[320px] w-[320px] p-5 rounded-2xl text-left flex flex-col justify-between transition-all duration-200 border-2 relative overflow-hidden group
                                {selectedSeason === season 
                                    ? 'bg-[#fff0e6] border-[#f54901] shadow-md' 
                                    : 'bg-white border-gray-100 hover:border-orange-200 shadow-sm'}"
                            >
                                <div class="absolute top-0 left-0 w-1 h-full bg-[#f54901] scale-y-0 group-hover:scale-y-100 transition-transform origin-top {selectedSeason === season ? 'scale-y-100' : ''}"></div>
                                
                                <h4 class="font-bold text-lg text-gray-800 line-clamp-2 leading-snug mb-4 group-hover:text-[#f54901] transition-colors">
                                    {season.name}
                                </h4>
                                
                                <div class="flex items-center justify-between mt-auto">
                                    <div class="flex items-center gap-2 text-gray-500 text-sm font-medium">
                                        <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                                        {formatFullDate(season.start_time)}
                                    </div>
                                    
                                    {#if selectedSeason === season}
                                        <div class="w-6 h-6 rounded-full bg-[#f54901] text-white flex items-center justify-center">
                                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"></path></svg>
                                        </div>
                                    {/if}
                                </div>
                            </button>
                        {/each}
                    </div>

                </div>
            {/if}
        </div>
    </section>

    <section class="py-20 px-[5%] bg-[var(--md-surface)] flex items-center min-h-[80vh]">
        <div class="max-w-[1250px] mx-auto grid grid-cols-1 md:grid-cols-2 gap-12 md:gap-16 items-center">
            <div>
                <h2 class="text-4xl md:text-[3.5rem] font-extrabold leading-[1.2] mb-4">
                    Shaping the Future<br/>
                    <span class="text-[#f54901]">Leading in Technology</span>
                </h2>
                <div class="w-[200px] h-1 bg-[#f54901] mb-6"></div>
                <p class="text-[var(--md-on-surface)] opacity-80 mb-[1.3rem] leading-relaxed">
                    SEAL Hackathon is more than just a coding competition; it's a launchpad for FPT University students to turn their boldest ideas into tangible solutions. Throughout this journey, you will be guided by experienced mentors, experience real-time pressure, and learn effective teamwork and collaboration.
                </p>
                <p class="text-[var(--md-on-surface)] opacity-80 mb-[1.3rem] leading-relaxed">
                    Beyond the competition, every participant not only gains an impressive milestone for their resume but also develops sharp problem-solving skills, ready to conquer the ever-evolving tech industry.
                </p>
                <a href="https://www.facebook.com/FPTHackathon" class="mt-6 px-8 py-4 font-semibold text-[1.3rem] rounded-[20px] cursor-pointer transition-all duration-200 border border-transparent inline-flex items-center gap-2 hover:scale-110 bg-[#f54901] text-white shadow-[0_4px_6px_rgba(245,73,1,0.2)] hover:bg-[#d64000]">
                    Learn More <span>&rarr;</span>
                </a>
            </div>

            <div class="relative mt-8 md:mt-0">
                <div class="absolute top-8 -bottom-8 left-4 right-0 md:left-6 md:-right-6 bg-[#fcece3] rounded-2xl z-[1]"></div>
                <div class="relative pb-[56.25%] h-0 overflow-hidden rounded-2xl shadow-[0_10px_30px_rgba(0,0,0,0.08)] z-[2] bg-black">
                    <iframe 
                        src="https://www.youtube.com/embed/LDSnUFNXCWg" 
                        title="SEAL Hackathon Video" 
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                        allowfullscreen
                        class="absolute top-0 left-0 w-full h-full border-none">
                    </iframe>
                </div>
            </div>
        </div>
    </section>
</div>

<style>
    /* Thanh cuộn ngang làm mềm mại hợp với thẻ bo tròn */
    .custom-scrollbar::-webkit-scrollbar {
        height: 8px;
    }
    .custom-scrollbar::-webkit-scrollbar-track {
        background: #fdfaf9; 
        border-radius: 10px;
    }
    .custom-scrollbar::-webkit-scrollbar-thumb {
        background: #e5e7eb; 
        border-radius: 10px;
    }
    .custom-scrollbar::-webkit-scrollbar-thumb:hover {
        background: #f54901; 
    }
</style>