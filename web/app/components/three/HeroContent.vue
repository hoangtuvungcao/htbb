<script setup lang="ts">
import { computed, shallowRef } from 'vue'
import type { Group, Mesh, Points } from 'three'
import { AdditiveBlending, BufferGeometry, Color, DoubleSide, Float32BufferAttribute } from 'three'

const props = defineProps<{ still?: boolean }>()

const kiem = shallowRef<Group>()
const vong1 = shallowRef<Mesh>()
const vong2 = shallowRef<Mesh>()
const vong3 = shallowRef<Mesh>()
const tinhThe = shallowRef<Group>()
const hao = shallowRef<Mesh>()

const GOLD = new Color('#f9c02a')
const JADE = new Color('#20e0bf')

/**
 * Bố cục theo khung hình. Chữ của hero nằm bên TRÁI, nên trên màn hình ngang cả
 * cụm kiếm + trăng + vòng được dời sang phải (theo nửa bề ngang nhìn thấy ở mặt
 * phẳng z=0: 9 * tan(21°) * tỉ lệ khung) để không đè lên tiêu đề. Màn hình dọc
 * (điện thoại) thì chữ chiếm cả bề ngang: thu nhỏ cảnh, đẩy lên trên và lùi ra sau.
 */
const { sizes } = useTres()
const boCuc = computed(() => {
  const a = sizes.aspectRatio.value || 1.6
  if (a >= 1.15) {
    const nuaNgang = 3.45 * a
    return { pos: [Math.min(3.6, nuaNgang * 0.55), 0, 0] as [number, number, number], scale: a < 1.4 ? 0.85 : 1 }
  }
  return { pos: [0.9, 1.1, -1.5] as [number, number, number], scale: 0.72 }
})

// Sáu tinh thể ngọc bay vòng quanh kiếm, mỗi viên một quỹ đạo lệch nhau.
const crystals = Array.from({ length: 6 }, (_, i) => ({
  r: 2.6 + (i % 3) * 0.55,
  goc: (i / 6) * Math.PI * 2,
  cao: ((i % 2) ? 0.9 : -0.7) + (i % 3) * 0.2,
  co: 0.16 + (i % 3) * 0.05,
  toc: 0.22 + (i % 4) * 0.05,
  mau: i % 2 ? '#20e0bf' : '#fdd352',
}))

/** Vị trí tinh thể ở thời điểm t (dùng cả cho khung đầu tiên / cảnh đứng yên). */
function viTriTinhThe(i: number, t: number): [number, number, number] {
  const k = crystals[i]!
  const a = k.goc + t * k.toc
  return [Math.cos(a) * k.r, k.cao + Math.sin(t * 0.8 + i) * 0.25, Math.sin(a) * k.r * 0.55 - 0.4]
}

// Đom đóm vàng: 140 đốm sáng rải trong một khối hộp quanh kiếm.
const domDom = shallowRef<Points>()
const domDomGeo = new BufferGeometry()
{
  const n = 140
  const arr = new Float32Array(n * 3)
  for (let i = 0; i < n; i++) {
    // Rải đều bằng chuỗi giả ngẫu nhiên cố định — cảnh giống nhau mỗi lần tải.
    const a = Math.sin(i * 12.9898) * 43758.5453
    const b = Math.sin(i * 78.233) * 12345.6789
    const c = Math.sin(i * 39.425) * 24634.6345
    arr[i * 3] = ((a - Math.floor(a)) - 0.5) * 11
    arr[i * 3 + 1] = ((b - Math.floor(b)) - 0.5) * 7
    arr[i * 3 + 2] = ((c - Math.floor(c)) - 0.5) * 5
  }
  domDomGeo.setAttribute('position', new Float32BufferAttribute(arr, 3))
}
onUnmounted(() => domDomGeo.dispose())

const { onBeforeRender } = useLoop()

onBeforeRender(({ elapsed }) => {
  if (props.still) return
  const t = elapsed
  if (kiem.value) {
    kiem.value.position.y = Math.sin(t * 0.9) * 0.18
    kiem.value.rotation.y = Math.sin(t * 0.35) * 0.55
    kiem.value.rotation.z = 0.32 + Math.sin(t * 0.6) * 0.04
  }
  if (vong1.value) vong1.value.rotation.z = t * 0.08
  if (vong2.value) {
    vong2.value.rotation.z = -t * 0.05
    vong2.value.rotation.x = 1.25 + Math.sin(t * 0.3) * 0.08
  }
  if (vong3.value) vong3.value.rotation.y = t * 0.12
  if (domDom.value) {
    domDom.value.rotation.y = t * 0.03
    domDom.value.position.y = Math.sin(t * 0.4) * 0.15
  }
  if (hao.value) {
    const s = 1 + Math.sin(t * 1.4) * 0.04
    hao.value.scale.set(s, s, s)
  }
  if (tinhThe.value) {
    tinhThe.value.children.forEach((c, i) => {
      c.position.set(...viTriTinhThe(i, t))
      c.rotation.x = t * 0.7 + i
      c.rotation.y = t * 0.9 + i
    })
  }
})
</script>

<template>
  <TresAmbientLight :intensity="0.45" />
  <TresDirectionalLight :position="[4, 6, 5]" :intensity="1.6" color="#fff3c4" />

  <MouseParallax :factor="0.35" :ease="2.5" />

  <TresGroup :position="boCuc.pos" :scale="boCuc.scale">
    <!-- Hai đèn màu đi theo kiếm để ánh ngọc / ánh vàng luôn rọi đúng chỗ -->
    <TresPointLight :position="[-3, 1.5, 3]" :intensity="18" :distance="12" :color="JADE" />
    <TresPointLight :position="[3, -1, 2.5]" :intensity="14" :distance="10" :color="GOLD" />

    <!-- Vầng trăng vàng phía sau -->
    <TresMesh :position="[1.6, 1.1, -6]">
      <TresSphereGeometry :args="[2.6, 64, 64]" />
      <TresMeshBasicMaterial color="#e8a50f" :transparent="true" :opacity="0.55" />
    </TresMesh>
    <TresMesh ref="hao" :position="[1.6, 1.1, -6.2]">
      <TresSphereGeometry :args="[3.6, 48, 48]" />
      <TresMeshBasicMaterial color="#f9c02a" :transparent="true" :opacity="0.08" :blending="AdditiveBlending" :depth-write="false" />
    </TresMesh>

    <!-- Vòng thiên văn -->
    <TresMesh ref="vong1" :position="[0, 0, -1.5]">
      <TresTorusGeometry :args="[3.3, 0.012, 16, 220]" />
      <TresMeshBasicMaterial color="#fdd352" :transparent="true" :opacity="0.55" />
    </TresMesh>
    <TresMesh ref="vong2" :position="[0, 0, -1.2]" :rotation="[1.25, 0, 0]">
      <TresTorusGeometry :args="[2.7, 0.018, 16, 200]" />
      <TresMeshStandardMaterial color="#20e0bf" :emissive="JADE" :emissive-intensity="1.2" :transparent="true" :opacity="0.7" />
    </TresMesh>
    <TresMesh ref="vong3" :position="[0, 0, -1.4]" :rotation="[0.2, 0, 0.6]">
      <TresRingGeometry :args="[3.8, 3.84, 160]" />
      <TresMeshBasicMaterial color="#ffe58a" :transparent="true" :opacity="0.25" :side="DoubleSide" />
    </TresMesh>

    <!-- Linh Kiếm -->
    <TresGroup ref="kiem" :rotation="[0, 0, 0.32]" :scale="1.05">
      <!-- lưỡi kiếm -->
      <TresMesh :position="[0, 0.55, 0]">
        <TresBoxGeometry :args="[0.2, 3.4, 0.05]" />
        <TresMeshStandardMaterial color="#d8fbff" :metalness="0.95" :roughness="0.12" :emissive="JADE" :emissive-intensity="0.35" />
      </TresMesh>
      <!-- mũi kiếm -->
      <TresMesh :position="[0, 2.38, 0]" :rotation="[0, Math.PI / 4, 0]">
        <TresConeGeometry :args="[0.142, 0.3, 4]" />
        <TresMeshStandardMaterial color="#d8fbff" :metalness="0.95" :roughness="0.12" :emissive="JADE" :emissive-intensity="0.35" />
      </TresMesh>
      <!-- sống kiếm phát sáng -->
      <TresMesh :position="[0, 0.6, 0.03]">
        <TresBoxGeometry :args="[0.025, 3.2, 0.01]" />
        <TresMeshBasicMaterial color="#c8fff1" />
      </TresMesh>
      <!-- hào quang lưỡi -->
      <TresMesh :position="[0, 0.6, 0]">
        <TresBoxGeometry :args="[0.5, 3.7, 0.02]" />
        <TresMeshBasicMaterial color="#20e0bf" :transparent="true" :opacity="0.12" :blending="AdditiveBlending" :depth-write="false" />
      </TresMesh>
      <!-- chuôi bảo vệ -->
      <TresMesh :position="[0, -1.18, 0]">
        <TresBoxGeometry :args="[1.15, 0.13, 0.16]" />
        <TresMeshStandardMaterial color="#f9c02a" :metalness="1" :roughness="0.25" />
      </TresMesh>
      <TresMesh :position="[-0.6, -1.12, 0]" :rotation="[0, 0, 0.6]">
        <TresConeGeometry :args="[0.09, 0.32, 6]" />
        <TresMeshStandardMaterial color="#f9c02a" :metalness="1" :roughness="0.25" />
      </TresMesh>
      <TresMesh :position="[0.6, -1.12, 0]" :rotation="[0, 0, -0.6]">
        <TresConeGeometry :args="[0.09, 0.32, 6]" />
        <TresMeshStandardMaterial color="#f9c02a" :metalness="1" :roughness="0.25" />
      </TresMesh>
      <!-- ngọc giữa chuôi -->
      <TresMesh :position="[0, -1.18, 0.1]">
        <TresOctahedronGeometry :args="[0.14, 0]" />
        <TresMeshStandardMaterial color="#20e0bf" :emissive="JADE" :emissive-intensity="1.6" :metalness="0.3" :roughness="0.1" />
      </TresMesh>
      <!-- cán -->
      <TresMesh :position="[0, -1.7, 0]">
        <TresCylinderGeometry :args="[0.065, 0.075, 0.9, 16]" />
        <TresMeshStandardMaterial color="#3a1d12" :roughness="0.6" />
      </TresMesh>
      <TresMesh v-for="i in 5" :key="i" :position="[0, -1.36 - i * 0.14, 0]">
        <TresTorusGeometry :args="[0.078, 0.014, 8, 24]" />
        <TresMeshStandardMaterial color="#c47f09" :metalness="1" :roughness="0.3" />
      </TresMesh>
      <!-- quả chuôi -->
      <TresMesh :position="[0, -2.22, 0]">
        <TresSphereGeometry :args="[0.11, 24, 24]" />
        <TresMeshStandardMaterial color="#f9c02a" :metalness="1" :roughness="0.2" />
      </TresMesh>
    </TresGroup>

    <!-- Tinh thể ngọc bay quanh -->
    <TresGroup ref="tinhThe">
      <TresMesh v-for="(k, i) in crystals" :key="i" :position="viTriTinhThe(i, 0)" :rotation="[i, i, 0]">
        <TresOctahedronGeometry :args="[k.co, 0]" />
        <TresMeshStandardMaterial :color="k.mau" :emissive="k.mau" :emissive-intensity="0.9" :metalness="0.4" :roughness="0.15" :flat-shading="true" />
      </TresMesh>
    </TresGroup>
  </TresGroup>

  <!-- Đom đóm vàng + sao trời: phủ cả khung, không dời theo bố cục -->
  <TresPoints ref="domDom" :geometry="domDomGeo">
    <TresPointsMaterial color="#fdd352" :size="0.06" :size-attenuation="true" :transparent="true" :opacity="0.9" :blending="AdditiveBlending" :depth-write="false" />
  </TresPoints>
  <Stars :radius="60" :depth="40" :count="1600" :size="0.18" :size-attenuation="true" />
</template>
