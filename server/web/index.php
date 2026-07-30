<?php
require_once __DIR__ . '/inc/functions.php';
$page = 'home';

$classNames = ['Đại phu', 'Đao khách', 'Kiếm khách', 'Cung thủ', 'Đấu sĩ'];

// BXH cấp độ (theo kinh nghiệm)
$topLevel = [];
try {
    $topLevel = dbGame()->query(
        'SELECT charname, lastLv, class FROM tob_char ORDER BY xp DESC, lastLv DESC LIMIT 10'
    )->fetchAll();
} catch (Throwable $ex) { $topLevel = []; }

// BXH nạp/ủng hộ (board_created)
$topNap = [];
try {
    $topNap = dbGame()->query(
        'SELECT username, luong FROM board_created ORDER BY luong DESC LIMIT 10'
    )->fetchAll();
} catch (Throwable $ex) { $topNap = []; }

// Tin tức tĩnh (bạn có thể sửa)
$news = [
    ['date' => '20/07', 'title' => 'Khai mở máy chủ mới', 'body' => 'Chào mừng anh hùng đến với thế giới Khí Phách Anh Hùng. Đăng ký ngay để nhận quà tân thủ!'],
    ['date' => '18/07', 'title' => 'Sự kiện Hè rực lửa', 'body' => 'Nạp càng nhiều, quà càng khủng. Xem chi tiết trong game.'],
    ['date' => '15/07', 'title' => 'Hướng dẫn tân thủ', 'body' => 'Tải game, đăng ký tài khoản và tạo nhân vật đầu tiên chỉ trong 1 phút.'],
];

require __DIR__ . '/inc/header.php';
?>
<section class="hero">
    <div class="hero-inner">
        <h1 class="hero-title">KHÍ PHÁCH ANH HÙNG</h1>
        <p class="hero-tagline"><?= e(SITE_TAGLINE) ?></p>
        <p class="hero-desc">Săn boss, lập bang hội, đua top và giao tranh PvP — huyền thoại nhập vai kiếm hiệp trở lại.</p>
        <div class="hero-actions">
            <?php if (!currentUser()): ?>
                <a href="register.php" class="btn btn-primary btn-lg">Đăng ký ngay</a>
            <?php endif; ?>
            <a href="#download" class="btn btn-outline btn-lg">Tải game</a>
        </div>
    </div>
</section>

<section id="download" class="section">
    <h2 class="section-title">Tải game</h2>
    <div class="download-grid">
        <a class="dl-card" href="<?= e(LINK_ANDROID) ?>"><span class="dl-ico">🤖</span><strong>Android</strong><small>Bản APK</small></a>
        <a class="dl-card" href="<?= e(LINK_PC) ?>"><span class="dl-ico">💻</span><strong>PC / Windows</strong><small>Giả lập</small></a>
        <a class="dl-card" href="<?= e(LINK_IOS) ?>"><span class="dl-ico">🍎</span><strong>iOS</strong><small>Cài đặt</small></a>
        <a class="dl-card" href="<?= e(LINK_GROUP) ?>"><span class="dl-ico">💬</span><strong>Nhóm hỗ trợ</strong><small>Zalo / Facebook</small></a>
    </div>
</section>

<section id="moc-nap" class="section">
    <h2 class="section-title">3 Mốc Tân Thủ</h2>
    <p class="muted" style="text-align:center;margin-bottom:26px;">
        Nạp mốc bất kỳ, vào game gặp <strong>NPC Tổng Quản</strong> chọn <strong>"Nhận mốc nạp"</strong> để nhận quà.
    </p>
    <div class="moc-grid">
        <?php foreach (mocNapList() as $moc => $info): ?>
            <div class="card moc-card">
                <h3 class="moc-title">Mốc <?= $moc ?></h3>
                <div class="moc-gia"><?= number_format($info['gia']) ?>đ</div>
                <ul class="moc-items">
                    <?php foreach ($info['items'] as $it): ?>
                        <li><?= e($it) ?></li>
                    <?php endforeach; ?>
                </ul>
                <a href="naptien.php" class="btn btn-primary btn-block">Nạp mốc <?= $moc ?></a>
            </div>
        <?php endforeach; ?>
    </div>
</section>

<section id="rank" class="section">
    <h2 class="section-title">Bảng xếp hạng</h2>
    <div class="rank-grid">
        <div class="card">
            <h3 class="card-head">🏆 Top Cấp Độ</h3>
            <?php if ($topLevel): ?>
                <table class="rank-table">
                    <thead><tr><th>#</th><th>Nhân vật</th><th>Lớp</th><th>Cấp</th></tr></thead>
                    <tbody>
                    <?php foreach ($topLevel as $i => $c): ?>
                        <tr>
                            <td class="rank-no rank-<?= $i < 3 ? ($i + 1) : '' ?>"><?= $i + 1 ?></td>
                            <td><?= e($c['charname']) ?></td>
                            <td><?= e($classNames[(int)$c['class']] ?? '—') ?></td>
                            <td><?= (int)$c['lastLv'] ?></td>
                        </tr>
                    <?php endforeach; ?>
                    </tbody>
                </table>
            <?php else: ?>
                <p class="muted">Đang cập nhật...</p>
            <?php endif; ?>
        </div>
        <div class="card">
            <h3 class="card-head">💎 Top Ủng Hộ</h3>
            <?php if ($topNap): ?>
                <table class="rank-table">
                    <thead><tr><th>#</th><th>Tài khoản</th><th>Lượng</th></tr></thead>
                    <tbody>
                    <?php foreach ($topNap as $i => $c): ?>
                        <tr>
                            <td class="rank-no rank-<?= $i < 3 ? ($i + 1) : '' ?>"><?= $i + 1 ?></td>
                            <td><?= e($c['username']) ?></td>
                            <td><?= number_format((int)$c['luong']) ?></td>
                        </tr>
                    <?php endforeach; ?>
                    </tbody>
                </table>
            <?php else: ?>
                <p class="muted">Đang cập nhật...</p>
            <?php endif; ?>
        </div>
    </div>
</section>

<section id="news" class="section">
    <h2 class="section-title">Tin tức &amp; Sự kiện</h2>
    <div class="news-list">
        <?php foreach ($news as $n): ?>
            <article class="news-item">
                <span class="news-date"><?= e($n['date']) ?></span>
                <div>
                    <h3><?= e($n['title']) ?></h3>
                    <p><?= e($n['body']) ?></p>
                </div>
            </article>
        <?php endforeach; ?>
    </div>
</section>
<?php require __DIR__ . '/inc/footer.php'; ?>
