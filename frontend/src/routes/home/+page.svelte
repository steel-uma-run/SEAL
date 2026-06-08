<script>
    // Trang chủ
</script>

<div class="startpage-content">
    <section class="hero-section">
        <div class="hero-bg"></div>
        
        <div class="hero-content">
            <div class="badge">Khởi nguồn sáng tạo, kiến tạo tương lai</div>
            
            <h1 class="hero-title">
                <span class="text-orange">SEAL</span> HACKATHON
            </h1>
            
            <p class="hero-desc">
                Hệ thống quản lý sự kiện và hỗ trợ sinh viên trường Đại học FPT. Đăng ký ngay để thành lập đội thi, nộp sản phẩm và trải nghiệm nền tảng!
            </p>
            
            <div class="button-group">
                <a href="/register" class="btn btn-solid">
                    Tham gia ngay
                </a>
                <a href="/login" class="btn btn-outline">
                    Dành cho sinh viên đã có đội
                </a>
            </div>
            

        </div>
    </section>

    <section class="feature-section">
        <div class="feature-container">
            <div class="feature-text">
                <h2>
                    Kiến tạo tương lai<br/>
                    <span class="text-orange">Dẫn đầu công nghệ</span>
                </h2>
                <div class="divider"></div>
                
                <p>
                    SEAL Hackathon không chỉ là một cuộc thi lập trình, mà là bệ phóng để các bạn sinh viên FPT University biến những ý tưởng điên rồ nhất thành sản phẩm thực tế. Xuyên suốt hành trình, bạn sẽ được dẫn dắt bởi đội ngũ mentor dày dặn kinh nghiệm, trải nghiệm áp lực thời gian thực, và học cách làm việc nhóm hiệu quả.
                </p>
                <p>
                    Bước ra khỏi cuộc thi, mỗi thí sinh không chỉ mang theo một "chiến tích" ấn tượng trong CV, mà còn tích lũy được tư duy giải quyết vấn đề sắc bén, sẵn sàng chinh phục thế giới công nghệ không ngừng đổi mới.
                </p>

                <a href="https://www.facebook.com/FPTHackathon" class="btn btn-solid btn-learn-more mt-4">
                    Tìm hiểu ngay <span class="arrow">&rarr;</span>
                </a>
            
            </div>

            <div class="feature-media">
                <div class="media-backdrop"></div>
                <div class="video-wrapper">
                    <iframe 
                        src="https://www.youtube.com/embed/LDSnUFNXCWg" 
                        title="SEAL Hackathon Video" 
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                        allowfullscreen>
                    </iframe>
                </div>
            </div>
        </div>


    </section>
</div>

<style>

    :root {
        --fpt-orange: #f54901;
        --fpt-orange-hover: #d64000;
        --md-on-surface: #202124;
        --md-surface: #ffffff;
    }

    .text-orange { color: #f54901; }
    .mt-4 { margin-top: 1.5rem; }

    .btn {
        padding: 1.2rem 2.4rem;
        font-weight: 600;
        font-size: 1.3rem;
        border-radius: 12px;
        cursor: pointer;
        transition: all 0.2s ease;
        border: 1px solid transparent;
        font-family: inherit;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        justify-content: center;
    }

    .btn:hover {
        transform: scale(1.1);
    }

    .btn-solid {
        background-color: var(--fpt-orange);
        color: #ffffff;
        box-shadow: 0 4px 6px rgba(245, 73, 1, 0.2);
    }

    .btn-solid:hover { background-color: var(--fpt-orange-hover); }

    .btn-outline {
        background-color: transparent;
        color: var(--fpt-orange);
        border-color: var(--fpt-orange);
    }

    .btn-outline:hover { background-color: rgba(245, 73, 1, 0.05); }

    /* --- HERO SECTION --- */
    .hero-section {
        position: relative;
        min-height: 92vh;
        display: flex;
        align-items: center;
        justify-content: center;
        text-align: center;
        padding: 4rem 1rem;
        background-color: #ffffff;

        background-image: url('src/image/37b363bc-8b90-469e-b18c-c07e2b8b116a.jpg');
        background-size: cover;       
        background-position: center;  
        background-repeat: no-repeat;      
    }

    .hero-section::after {
        content: "";
        position: absolute;
        bottom: 0;
        left: 0;
        width: 100%;
        height: 400px;
        background: linear-gradient(to bottom, rgba(253, 250, 249, 0) 0%, #fdfaf9 100%);
        z-index: 1;
        pointer-events: none;
    }

    .hero-content {
        position: relative;
        z-index: 2;
        max-width: 700px;
    }

    .badge {
        display: inline-block;
        background-color: #fff0e6;
        color: var(--fpt-orange);
        padding: 0.6rem 1.2rem;
        border-radius: 50px;
        font-size: 1rem;
        font-weight: 600;
        margin-bottom: 1.5rem;
    }

    .hero-title {
        font-size: 3.5rem;
        font-weight: 900;
        margin: 0 0 1rem 0;
        color: var(--md-on-surface);
    }

    .hero-desc {
        font-size: 1.3rem;
        color: var(--md-on-surface);
        opacity: 0.8;
        margin-bottom: 2rem;
        line-height: 1.6;
    }

    .button-group {
        display: flex;
        gap: 2rem;
        justify-content: center;
    }

    /* --- FEATURE SECTION --- */
    .feature-section {
        padding: 5rem 5%;
        background-color: var(--md-surface);
        display: flex;
        align-items: center;
        min-height: 100vh;
    }

    .feature-container {
        max-width: 1250px;
        margin: 0 auto;
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 4rem;
        align-items: center;
    }

    .feature-text h2 {
        font-size: 3.5rem;
        font-weight: 800;
        line-height: 1.2;
        margin-bottom: 1rem;
    }

    .divider {
        width: 200px;
        height: 4px;
        background-color: var(--fpt-orange);
        margin-bottom: 1.5rem;
    }

    .feature-text p {
        color: var(--md-on-surface);
        opacity: 0.8;
        margin-bottom: 1.3rem;
        line-height: 1.6;
    }

    .btn-learn-more {
        margin-top: 1rem;
        padding: 1rem 2rem;
        border-radius: 20px; 
        display: inline-flex;
        align-items: center;
        gap: 0.5rem;
    }

    .feature-media {
        position: relative;
    }

    .media-backdrop {
        position: absolute;
        top: 2rem;
        right: -1.5rem;
        bottom: -2rem;
        left: 1.5rem;
        background-color: #fcece3;
        border-radius: 16px;
        z-index: 1;
    }

    .video-wrapper {
        position: relative;
        padding-bottom: 56.25%; 
        height: 0;
        overflow: hidden;
        border-radius: 16px; /* Bo góc video cho mềm mại */
        box-shadow: 0 10px 30px rgba(0,0,0,0.08); /* Đổ bóng nhẹ */
        z-index: 2; /* Đảm bảo video nằm trên lớp backdrop */
        background-color: #000;
    }

    .video-wrapper iframe {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        border: none;
    }

    /* Responsive cho điện thoại */
    @media (max-width: 768px) {
        .feature-container {
            grid-template-columns: 1fr;
            gap: 3rem;
        }
        .hero-title { font-size: 2.5rem; }
        .button-group { flex-direction: column; }
        .media-backdrop { right: 0; left: 1rem; }
    }
</style>