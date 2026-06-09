<script>
    // Homepage
</script>

<div class="startpage-content">
    <section class="hero-section">
        <div class="hero-bg">
            <img src="https://img.freepik.com/free-vector/futuristic-white-technology-background_23-2148390336.jpg" alt="Background pattern" class="bg-img" />
        </div>
        
        <div class="hero-content">
            <div class="badge">Inspiring Innovation, Shaping the Future</div>
            
            <h1 class="hero-title">
                <span class="text-orange">SEAL</span> HACKATHON
            </h1>
            
            <p class="hero-desc">
                The official event management and student support platform for FPT University. Register now to form your team, submit your project, and experience the platform!
            </p>
            
            <div class="button-group">
                <a href="/auth/register" class="btn btn-solid">
                    Join Now
                </a>
                <a href="/auth/login" class="btn btn-outline">
                    For Registered Teams
                </a>
            </div>
        </div>
    </section>

    <section class="feature-section">
        <div class="feature-container">
            <div class="feature-text">
                <h2>
                    Shaping the Future<br/>
                    <span class="text-orange">Leading in Technology</span>
                </h2>
                <div class="divider"></div>
                
                <p>
                    SEAL Hackathon is more than just a coding competition; it's a launchpad for FPT University students to turn their boldest ideas into tangible solutions. Throughout this journey, you will be guided by experienced mentors, experience real-time pressure, and learn effective teamwork and collaboration.
                </p>
                <p>
                    Beyond the competition, every participant not only gains an impressive milestone for their resume but also develops sharp problem-solving skills, ready to conquer the ever-evolving tech industry.
                </p>

                <a href="https://www.facebook.com/FPTHackathon" class="btn btn-solid btn-learn-more mt-4">
                    Learn More <span class="arrow">&rarr;</span>
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
    }

    .bg-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        opacity: 0.7;
        transition: all 0.3s ease;
    }

    :global([data-theme='dark']) .bg-img {
        filter: invert(1) hue-rotate(180deg);
        opacity: 0.2; 
    }

    .hero-bg {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        z-index: 0;
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
        border-radius: 16px;
        box-shadow: 0 10px 30px rgba(0,0,0,0.08);
        z-index: 2;
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
