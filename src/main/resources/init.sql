alter table public.task
    add constraint unique_task_number_per_course unique (course_id, task_number)
        DEFERRABLE INITIALLY DEFERRED;