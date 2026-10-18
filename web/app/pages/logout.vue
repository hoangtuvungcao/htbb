<script setup lang="ts">
/**
 * Đăng xuất. Phiên là token trong trình duyệt nên đăng xuất =
 * xoá token phía client; không cần gọi server, cũng không cần token CSRF.
 *
 * Ba trạng thái:
 *   - đang đăng nhập  -> hỏi xác nhận
 *   - vừa đăng xuất   -> "Đã đăng xuất" (?done=1)
 *   - chưa đăng nhập  -> "Bạn chưa đăng nhập"
 */
const tenSite = useAppConfig().htbb.tenSite
useSeoMeta({
  title: 'Đăng xuất',
  description: `Kết thúc phiên đăng nhập trên website ${tenSite}.`,
  robots: 'noindex',
})

const auth = useAuth()
const route = useRoute()
const toast = useToast()
onMounted(() => auth.hydrate())

const logoutUser = computed(() => auth.user.value)
const logoutComplete = computed(() => !logoutUser.value && route.query.done === '1')

async function dangXuat() {
  auth.clear()
  // Xoá cache trang Tài khoản để người dùng sau không thấy dữ liệu cũ.
  clearNuxtData(['htbb-account', 'htbb-kichhoat'])
  toast.add({ title: 'Đã đăng xuất. Hẹn gặp lại bạn.', color: 'success', icon: 'i-lucide-log-out' })
  await navigateTo({ path: '/logout', query: { done: '1' } }, { replace: true })
}
</script>

<template>
  <SiteAuthShell>
    <div
      class="mb-6 grid size-14 place-items-center rounded-2xl border text-2xl"
      :class="logoutUser
        ? 'border-primary/30 bg-primary/10 text-primary shadow-[0_0_30px_rgba(10,26,51,.14)]'
        : 'border-success/40 bg-success/10 text-success shadow-[0_0_30px_rgba(7,196,166,.25)]'"
      aria-hidden="true"
    >
      <UIcon :name="logoutUser ? 'i-lucide-moon' : 'i-lucide-check'" class="size-7" />
    </div>
    <p class="eyebrow">
      {{ logoutUser ? 'Tạm biệt, không phải chia xa' : 'Thế giới Pokémon luôn chào đón bạn' }}
    </p>
    <h1 id="logout-title" class="section-title mt-3 text-4xl text-highlighted">
      <template v-if="logoutUser">
        Hẹn ngày<br><em class="text-gilded">tái ngộ.</em>
      </template>
      <template v-else-if="logoutComplete">
        Đã đăng xuất.<br><em class="text-gilded">Hẹn gặp lại bạn.</em>
      </template>
      <template v-else>
        Bạn chưa<br><em class="text-gilded">đăng nhập.</em>
      </template>
    </h1>

    <template v-if="logoutUser">
      <p class="mt-3 text-muted">
        Bạn muốn kết thúc phiên đăng nhập trên website? Bạn bè và hành trình vẫn chờ bạn trở lại.
      </p>
      <div class="mt-6 flex items-center gap-4 rounded-2xl border border-default bg-elevated/60 p-4">
        <span class="grid size-11 place-items-center rounded-full bg-primary/10 text-primary" aria-hidden="true">
          <UIcon name="i-lucide-user-round" class="size-5" />
        </span>
        <div class="min-w-0 flex-1">
          <small class="block text-[0.7rem] tracking-[0.2em] text-dimmed">TÀI KHOẢN ĐANG ĐĂNG NHẬP</small>
          <strong class="block truncate text-highlighted">{{ logoutUser.username }}</strong>
        </div>
        <span class="size-2.5 rounded-full bg-success shadow-[0_0_10px_var(--color-grass-400)] animate-glow" aria-hidden="true" />
      </div>
      <div class="mt-6 flex flex-col gap-3">
        <UButton size="xl" block trailing-icon="i-lucide-arrow-up-right" @click="dangXuat">
          Đăng xuất
        </UButton>
        <UButton to="/account" color="neutral" variant="outline" size="xl" block>
          Ở lại thêm chút nữa
        </UButton>
      </div>
      <p class="mt-6 text-center text-xs tracking-widest text-dimmed">
        Thao tác này chỉ đăng xuất khỏi website.
      </p>
    </template>

    <template v-else>
      <p class="mt-3 text-muted">
        {{ logoutComplete
          ? 'Phiên đăng nhập trên website đã kết thúc. Bạn có thể đăng nhập lại bất cứ khi nào muốn tiếp tục.'
          : 'Đăng nhập để quản lý tài khoản hoặc trở về trang chủ để khám phá thế giới Pokémon.' }}
      </p>
      <div class="mt-6 flex flex-col gap-3">
        <UButton to="/login" size="xl" block trailing-icon="i-lucide-arrow-up-right">
          Đăng nhập{{ logoutComplete ? ' lại' : '' }}
        </UButton>
        <UButton to="/" color="neutral" variant="outline" size="xl" block>
          Về trang chủ
        </UButton>
      </div>
      <p class="mt-6 text-center text-xs tracking-widest text-dimmed">
        Một lời chào tạm biệt. Còn nhiều hành trình phía trước.
      </p>
    </template>
  </SiteAuthShell>
</template>
