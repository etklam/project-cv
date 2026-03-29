<script setup>
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { useAuthStore } from "@/stores/auth";
import { skipStep2, submitStep2 } from "@/api/onboarding";

const { t } = useI18n();
const router = useRouter();
const auth = useAuthStore();

const form = ref({
  industry: "Technology & Software",
  years: "3-5 Years (Mid)",
  targetTitle: "",
  leadership: "individual",
});

const loading = ref(false);
const experienceProfiles = {
  "0-2 Years (Entry)": {
    badge: "Early career",
    headline: "Tell us what role you want to break into.",
    description:
      "For early-career candidates we focus on direction, momentum, and signals of learning speed rather than pure years shipped.",
    targetLabel: "What role are you trying to land first?",
    targetPlaceholder: "e.g. Junior Frontend Developer",
    leadershipLabel: "How do you want to contribute right now?",
    managerTitle: "Project ownership",
    managerDescription: "Own a stream, drive coordination, or take visible initiative.",
    individualTitle: "Hands-on execution",
    individualDescription: "Build craft depth and show strong delivery fundamentals.",
    why:
      "We bias the resume toward learning velocity, internships, side projects, and proof of initiative.",
  },
  "3-5 Years (Mid)": {
    badge: "Growth mode",
    headline: "Tell us where your next role should move you.",
    description:
      "For mid-level candidates we look for progression, sharper positioning, and evidence that your scope is expanding.",
    targetLabel: "What role are you growing into next?",
    targetPlaceholder: "e.g. Staff Product Designer",
    leadershipLabel: "Which scope best matches your current work?",
    managerTitle: "Team or stream lead",
    managerDescription: "Leading projects, mentoring peers, or owning cross-functional outcomes.",
    individualTitle: "Senior individual contributor",
    individualDescription: "Driving execution depth and shipping independently across ambiguous work.",
    why:
      "We tune the draft to highlight trajectory, sharper ownership, and the jump from execution to influence.",
  },
  "6-10 Years (Senior)": {
    badge: "Senior operator",
    headline: "Tell us what level of scope this resume should signal.",
    description:
      "Senior candidates need stronger framing around ownership, business impact, and the size of problems they can carry.",
    targetLabel: "What title or charter should this resume support?",
    targetPlaceholder: "e.g. Senior Engineering Manager",
    leadershipLabel: "What kind of leadership are you exercising today?",
    managerTitle: "People and strategy leader",
    managerDescription: "Managing managers, shaping roadmaps, or directing multi-team execution.",
    individualTitle: "Principal-level specialist",
    individualDescription: "Leading through technical judgment, architecture, and high-leverage delivery.",
    why:
      "We increase density around decisions, influence, and measurable outcomes instead of generic task lists.",
  },
  "10+ Years (Expert)": {
    badge: "Executive track",
    headline: "Tell us what mandate this resume needs to communicate.",
    description:
      "Experienced leaders need fewer generic details and stronger signaling around scale, transformation, and executive trust.",
    targetLabel: "What mandate, title, or portfolio are you targeting?",
    targetPlaceholder: "e.g. VP Product or Head of Design",
    leadershipLabel: "Which leadership lens should shape the story?",
    managerTitle: "Org leadership",
    managerDescription: "Owning vision, organizational design, and company-level priorities.",
    individualTitle: "Strategic expert",
    individualDescription: "Operating as a top specialist with outsized influence across teams or business units.",
    why:
      "We bias toward scale, transformation, and senior decision-making rather than day-to-day execution detail.",
  },
};

const currentProfile = computed(
  () => experienceProfiles[form.value.years] || experienceProfiles["3-5 Years (Mid)"],
);

const handleSubmit = async (e) => {
  e.preventDefault();
  loading.value = true;
  try {
    const data = await submitStep2(form.value);
    auth.applyUser(data.user);
    router.push("/onboarding/step3");
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const handleSkip = async () => {
  const data = await skipStep2();
  auth.applyUser(data.user);
  router.push("/onboarding/step3");
};
</script>

<template>
  <section data-testid="view-onboarding-step2" class="mx-auto w-full max-w-6xl space-y-8">
    <div class="grid gap-4 md:grid-cols-[minmax(0,1fr)_220px]">
      <div class="rounded-[30px] border border-outline-variant/30 bg-surface-container-low px-6 py-6">
        <span class="inline-flex w-fit items-center rounded-full bg-tertiary-fixed px-3 py-1 text-xs font-bold uppercase tracking-[0.22em] text-on-tertiary-fixed-variant">
          {{ t("onboarding.progress", { current: 2, total: 3 }) }}
        </span>
        <h2 class="mt-4 font-headline text-3xl font-extrabold tracking-tight text-on-surface sm:text-4xl">
          {{ currentProfile.headline }}
        </h2>
        <p class="mt-3 max-w-3xl text-sm leading-7 text-on-surface-variant">
          {{ currentProfile.description }}
        </p>
      </div>

      <div class="rounded-[30px] border border-outline-variant/30 bg-surface-container-lowest px-5 py-5 shadow-sm">
        <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-on-surface-variant">Calibration</p>
        <p class="mt-3 text-2xl font-extrabold tracking-tight text-on-surface" data-testid="onboarding-experience-badge">
          {{ currentProfile.badge }}
        </p>
        <p class="mt-2 text-sm leading-7 text-on-surface-variant">
          Questions adapt to your experience level so the resume starts from the right narrative.
        </p>
      </div>
    </div>

    <form @submit="handleSubmit" class="space-y-8">
      <div class="rounded-[32px] border border-outline-variant/30 bg-white/95 p-6 shadow-[0_24px_48px_rgba(15,23,42,0.04)] sm:p-7">
        <div class="grid gap-8 md:grid-cols-2">
          <label class="space-y-2">
            <span class="ml-1 text-xs font-bold uppercase tracking-[0.22em] text-on-surface-variant">Current Industry</span>
            <div class="relative">
              <select
                v-model="form.industry"
                data-testid="onboarding-industry-select"
                class="h-14 w-full appearance-none rounded-[24px] border border-outline-variant/40 bg-surface-container-low px-4 pr-12 text-sm font-medium text-on-surface transition focus:border-primary focus:bg-white focus:outline-none focus:ring-4 focus:ring-primary/10"
              >
                <option>Technology &amp; Software</option>
                <option>Creative &amp; Design</option>
                <option>Healthcare &amp; Biotech</option>
                <option>Finance &amp; Fintech</option>
                <option>Education</option>
              </select>
              <span class="material-symbols-outlined pointer-events-none absolute right-4 top-1/2 -translate-y-1/2 text-on-surface-variant">expand_more</span>
            </div>
          </label>

          <label class="space-y-2">
            <span class="ml-1 text-xs font-bold uppercase tracking-[0.22em] text-on-surface-variant">Years of Experience</span>
            <div class="relative">
              <select
                v-model="form.years"
                data-testid="onboarding-years-select"
                class="h-14 w-full appearance-none rounded-[24px] border border-outline-variant/40 bg-surface-container-low px-4 pr-12 text-sm font-medium text-on-surface transition focus:border-primary focus:bg-white focus:outline-none focus:ring-4 focus:ring-primary/10"
              >
                <option>0-2 Years (Entry)</option>
                <option>3-5 Years (Mid)</option>
                <option>6-10 Years (Senior)</option>
                <option>10+ Years (Expert)</option>
              </select>
              <span class="material-symbols-outlined pointer-events-none absolute right-4 top-1/2 -translate-y-1/2 text-on-surface-variant">expand_more</span>
            </div>
          </label>
        </div>

        <label class="mt-8 block space-y-2">
          <span class="ml-1 text-xs font-bold uppercase tracking-[0.22em] text-on-surface-variant" data-testid="onboarding-target-label">
            {{ currentProfile.targetLabel }}
          </span>
          <input
            v-model="form.targetTitle"
            data-testid="onboarding-target-title"
            type="text"
            :placeholder="currentProfile.targetPlaceholder"
            class="h-14 w-full rounded-[24px] border border-outline-variant/40 bg-surface-container-low px-4 text-sm font-medium text-on-surface placeholder:text-outline transition focus:border-primary focus:bg-white focus:outline-none focus:ring-4 focus:ring-primary/10"
          />
        </label>

        <div class="mt-8 space-y-4">
          <p class="ml-1 text-xs font-bold uppercase tracking-[0.22em] text-on-surface-variant" data-testid="onboarding-leadership-label">
            {{ currentProfile.leadershipLabel }}
          </p>
          <div class="grid gap-4 md:grid-cols-2">
            <label
              class="group flex cursor-pointer items-center gap-4 rounded-[24px] border p-4 transition-all"
              :class="form.leadership === 'manager' ? 'border-primary bg-primary-fixed/30' : 'border-outline-variant/30 bg-surface-container-lowest hover:border-primary/40'"
            >
              <input
                v-model="form.leadership"
                value="manager"
                data-testid="onboarding-leadership-manager"
                class="h-5 w-5 border-outline-variant bg-surface text-primary focus:ring-primary"
                name="leadership"
                type="radio"
              />
              <div>
                <span class="block font-bold text-on-surface group-hover:text-primary">{{ currentProfile.managerTitle }}</span>
                <span class="text-xs text-on-surface-variant">{{ currentProfile.managerDescription }}</span>
              </div>
            </label>

            <label
              class="group flex cursor-pointer items-center gap-4 rounded-[24px] border p-4 transition-all"
              :class="form.leadership === 'individual' ? 'border-primary bg-primary-fixed/30' : 'border-outline-variant/30 bg-surface-container-lowest hover:border-primary/40'"
            >
              <input
                v-model="form.leadership"
                value="individual"
                data-testid="onboarding-leadership-individual"
                class="h-5 w-5 border-outline-variant bg-surface text-primary focus:ring-primary"
                name="leadership"
                type="radio"
              />
              <div>
                <span class="block font-bold text-on-surface group-hover:text-primary">{{ currentProfile.individualTitle }}</span>
                <span class="text-xs text-on-surface-variant">{{ currentProfile.individualDescription }}</span>
              </div>
            </label>
          </div>
        </div>
      </div>

      <div class="grid gap-4 md:grid-cols-5">
        <div class="relative overflow-hidden rounded-[32px] bg-gradient-to-br from-primary-container to-primary p-8 md:col-span-3">
          <div class="relative z-10 max-w-md space-y-3">
            <h3 class="font-headline text-2xl font-extrabold tracking-tight text-white">Why this matters?</h3>
            <p class="text-sm leading-7 text-on-primary-container" data-testid="onboarding-why-copy">
              {{ currentProfile.why }}
            </p>
          </div>
          <div class="absolute -bottom-5 -right-5 opacity-20">
            <span class="material-symbols-outlined text-[8rem] text-white" style="font-variation-settings: 'FILL' 1;">architecture</span>
          </div>
        </div>

        <div class="relative min-h-[200px] overflow-hidden rounded-[32px] border border-outline-variant/30 bg-surface-container-highest md:col-span-2">
          <img
            alt="Professional desk scene"
            class="h-full w-full object-cover grayscale opacity-55 mix-blend-multiply"
            src="https://lh3.googleusercontent.com/aida-public/AB6AXuDO9KuPSfJ_4XYbK1czWm8rurVZA56o-0Ndej6YDClLq5oDl6xir1Z2ADzxhx63b6VUsxhRNDbTwvZ20lbv2sQFwvAPBzwTeZ6BSai5IWXKvSdNEJbgZ7X9e3FwJqDHH3oexjCOlgrzmlo0heX-JnxryewKbwWWFw7AkQtgkfjxdrsNXJpr7y1kQX7eqtjQ7hBn5Um8PkqmYwbaqPcO11SNVY8LpZf01WOurAf7QT-whcB-xU_Wcb2iSU-1vilLz-FivyrMVopRYg"
          />
          <div class="absolute inset-x-0 bottom-0 p-6">
            <p class="text-[11px] font-bold uppercase tracking-[0.22em] text-on-surface-variant">Context matters</p>
            <p class="mt-2 text-sm font-semibold text-on-surface">The system changes the questions as your career stage changes.</p>
          </div>
        </div>
      </div>

      <div class="flex items-center justify-between gap-4 pt-2">
        <button
          type="button"
          @click="handleSkip"
          class="inline-flex h-12 items-center gap-2 rounded-full px-5 text-sm font-semibold text-on-surface-variant transition hover:bg-surface-container-high"
        >
          <span class="material-symbols-outlined text-base">arrow_back</span>
          {{ t("onboarding.back") }}
        </button>
        <button
          type="submit"
          data-testid="onboarding-step2-continue"
          :disabled="loading"
          class="inline-flex h-14 items-center gap-3 rounded-[20px] bg-gradient-to-br from-primary to-primary-container px-8 text-base font-bold text-on-primary shadow-lg transition hover:scale-[1.02] hover:shadow-xl disabled:cursor-not-allowed disabled:opacity-50"
        >
          {{ loading ? t("common.loading") : "Continue to Template" }}
          <span class="material-symbols-outlined text-base">arrow_forward</span>
        </button>
      </div>
    </form>
  </section>
</template>
