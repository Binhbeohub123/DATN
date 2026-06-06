import { onMounted, onUnmounted } from 'vue'

export function useReveal(root, selector = '.reveal', delayStep = 50) {
  let observer

  onMounted(() => {
    const targets = root.value?.querySelectorAll(selector) ?? []
    observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-visible')
            observer.unobserve(entry.target)
          }
        })
      },
      { threshold: 0.12 }
    )
    targets.forEach((el, i) => {
      el.style.transitionDelay = `${i * delayStep}ms`
      observer.observe(el)
    })
  })

  onUnmounted(() => observer?.disconnect())
}
