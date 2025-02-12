<script lang="ts">
    import Tooltip from "$lib/components/common/Tooltip.svelte";
    import {twMerge} from "tailwind-merge";
    import Transition from "svelte-transition";
    import DeleteButton from "./DeleteButton.svelte";
    import {Edit, Pin} from "lucide-svelte";
    import LoginPrompt from "./LoginPrompt.svelte";
    import {
        courseIdToUrlParam,
        determineReviewFor,
        instructorIdToName,
        spliceCourseCode,
        timeFrame
    } from "$lib/utils.js";
    import type {Review} from "$lib/model/Review";
    import type {Writable} from "svelte/store";
    import {writable} from "svelte/store";
    import {format} from 'date-fns';
    import IconRating from "./IconRating.svelte";
    import ReviewInteractions from "./ReviewInteractions.svelte";
    import type {Interaction} from "$lib/model/Interaction";
    import Share from "$lib/components/common/Share.svelte";
    import {onMount} from "svelte";

    export let canModify: boolean;
    export let title: string = '';
    export let handleDelete: () => void;
    export let editReview: Writable<boolean> = writable(false);
    export let interactions: Interaction[];
    export let updateLikes: (likes: number) => void;
    export let review: Review;
    export let shareable: boolean = true;

    let readMore = false;
    let show = false;
    const promptLogin = writable(false);
    let now: Date = new Date();
    let displayTime: string;

    $: shortDate = format(review.timestamp ?? Date.now(), 'P');
    $: longDate = format(review.timestamp ?? Date.now(), 'EEEE, MMMM d, yyyy');
    $: displayTime = timeFrame(new Date(review.timestamp), now);

    onMount(() => {
        const interval = setInterval(() => {
            now = new Date();
        }, 1000); // Update every second

        return () => {
            clearInterval(interval);
        };
    });

</script>

<div class={twMerge(
        'relative flex w-full flex-col gap-4 border-b-[1px] border-b-gray-300 bg-slate-50 px-6 py-3 first:rounded-t-md last:rounded-b-md last:border-b-0 dark:border-b-gray-600 dark:bg-neutral-800',
        $$props.class
      )}
>
    <div class='flex flex-col'>
        <div class='flex w-full'>
            <div class='relative flex w-full flex-col'>
                <div class='flex w-full'>
                    <div class="flex flex-col py-1">
                        {#if title}
                            <div>
                                <h3 class='text-lg font-semibold text-gray-800 dark:text-gray-200'>
                                    {title}
                                </h3>
                            </div>
                        {/if}
                        <Tooltip {show} text={longDate}>
                            <div class="flex flex-col gap-y-1">
                                <p class='cursor-default text-xs font-medium text-gray-700 dark:text-gray-300'
                                   on:mouseenter={() => show = true}
                                   on:mouseleave={() => show = false}>
                                    {shortDate}
                                </p>
                                <p class='cursor-default text-xs font-medium text-gray-700 dark:text-gray-300'>
                                    {displayTime}
                                </p>
                            </div>
                        </Tooltip>
                    </div>
                    {#if canModify}
                        <Pin class='ml-2 mt-2 text-primary-600'/>
                    {/if}
                    <div class='grow'/>
                    <div class='flex w-64 flex-col items-end rounded-lg p-2'>
                        <div class='flex items-center gap-x-2'>
                            <div class='whitespace-nowrap text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-400'>
                                {(review.type ?? 'course') === 'course' ? 'Experience' : 'Clarity Rating'}
                            </div>
                            <IconRating rating={(review.type ?? 'course') === 'course' ? review.experience : review.rating} icon="star"/>
                        </div>
                        <div class='flex items-center gap-x-2'>
                            <div class='text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-400'>
                                Difficulty
                            </div>
                            <IconRating rating={review.difficulty} icon="flame"/>
                        </div>
                        {#if review.userId.startsWith("rate_my_professor")}
                            <div class='mt-2 px-2 bg-primary-400 rounded-lg w-fit'>
                                from rate my professor
                            </div>
                        {:else if review.userId.startsWith("reddit")}
                            <div class='mt-2 px-2 bg-primary-400 rounded-lg w-fit'>
                                from reddit
                            </div>
                        {/if}
                    </div>
                </div>
                <div class='ml-1 mr-4 mt-2 hyphens-auto text-left text-gray-800 dark:text-gray-300'>
                    {#if review.content?.length < 300 || readMore}
                        {@html review.content.replace(/\n/g, '<br>')}
                    {:else }
                        {@html review.content.substring(0, 300).replace(/\n/g, '<br>') + '...'}
                    {/if}
                    {#if review.content?.length >= 300}
                        <button class='ml-1 mr-auto pt-1 text-gray-700 underline transition duration-300 ease-in-out hover:text-primary dark:text-gray-300 dark:hover:text-primary'
                                on:click={() => readMore = !readMore}>
                            Show {readMore ? 'less' : 'more'}
                        </button>
                    {/if}
                </div>
                {#if review?.tags}
                    <div class='flex flex-wrap gap-1 mt-1'>
                        {#each review.tags as tag}
                            <span class='px-2 py-1 text-xs font-medium text-gray-700 bg-gray-200 rounded-full dark:bg-gray-700 dark:text-gray-300'>
                                {tag}
                            </span>
                        {/each}
                    </div>
                {/if}
            </div>
        </div>
    </div>
    <div class='flex items-center'>
        <p class='mb-2 mt-auto flex-1 text-sm italic leading-4 text-gray-700 dark:text-gray-200'>
            {#if (review.type ?? 'course') === 'course'}
                Taught by{' '}
                <a href={`/instructor/${review.instructorId}`}
                   class='font-medium transition hover:text-primary-600'>
                    {instructorIdToName(review.instructorId)}
                </a>
            {:else }
                Written for{' '}
                <a href={`/course/${courseIdToUrlParam(review.courseId)}`}
                   class='font-medium transition hover:text-primary-600'>
                    {spliceCourseCode(review.courseId, ' ')}
                </a>
            {/if}
        </p>
        <Transition
                show={$promptLogin}
                enter='transition-opacity duration-150'
                enterFrom='opacity-0'
                enterTo='opacity-100'
                leave='transition-opacity duration-150'
                leaveFrom='opacity-100'
                leaveTo='opacity-0'
        >
            <LoginPrompt/>
        </Transition>
        <div class='flex items-center'>
            <div class='mb-1 flex'>
                {#if canModify}
                    <div class='ml-2 mr-1 flex h-fit space-x-2'>
                        <button on:click={() => editReview.set(true)}>
                            <Edit class='cursor-pointer stroke-gray-500 transition duration-200 hover:stroke-gray-800 dark:stroke-gray-400 dark:hover:stroke-gray-200'
                                  size={20}/>
                        </button>
                        <DeleteButton
                                title='Delete Review'
                                text={`Are you sure you want to delete your review  for ${(review.type ?? 'course') === 'course' ? instructorIdToName(review.instructorId) : spliceCourseCode(review.courseId, ' ')}? `}
                                onConfirm={handleDelete}
                                size={20}
                        />
                    </div>
                {/if}
            </div>
            <div class="flex gap-x-3">
                {#if updateLikes}
                    <ReviewInteractions
                            type={review.type ?? 'course'}
                            {review}
                            {interactions}
                            {promptLogin}
                            {updateLikes}
                    />
                {/if}
                {#if shareable}
                <Share sharedLink={`https://concordia.courses/shared?id=${review._id}`}
                       reviewFor={determineReviewFor(review)}/>
                    {/if}
            </div>
        </div>
    </div>
</div>