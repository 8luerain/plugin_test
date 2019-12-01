package com.xiaomi.shop.build.gradle.plugins.hooker.manager

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformTask
import com.xiaomi.shop.build.gradle.plugins.hooker.GradleTaskHooker
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState
import org.gradle.internal.reflect.Instantiator

public abstract class TaskHookerManager {

    protected Map<String, GradleTaskHooker> taskHookerMap = new HashMap<>()

    protected Project mProject
    protected AppExtension android
    protected Instantiator instantiator

    public TaskHookerManager(Project mProject, Instantiator instantiator) {
        this.mProject = mProject
        this.instantiator = instantiator
        android = mProject.extensions.findByType(AppExtension)
        mProject.gradle.addListener(new VirtualApkTaskListener())
    }

    public abstract void registerTaskHookers()

    protected void registerTaskHooker(GradleTaskHooker taskHooker) {
        taskHooker.setTaskHookerManager(this)
        taskHookerMap.put(taskHooker.taskName, taskHooker)
    }


    public <T> T findHookerByName(String taskName) {
        return taskHookerMap[taskName] as T
    }


    private class VirtualApkTaskListener implements TaskExecutionListener {

        @Override
        void beforeExecute(Task task) {
//            if (task.project == project) {
            if (task in TransformTask) {
                taskHookerMap["${task.transform.name}For${task.variantName.capitalize()}".toString()]?.beforeTaskExecute(task)
            } else {
                taskHookerMap[task.name]?.beforeTaskExecute(task)
            }
//            }
        }

        @Override
        void afterExecute(Task task, TaskState taskState) {

//            if (task.project == project) {
            if (task in TransformTask) {
                taskHookerMap["${task.transform.name}For${task.variantName.capitalize()}".toString()]?.afterTaskExecute(task)
            } else {
                taskHookerMap[task.name]?.afterTaskExecute(task)
            }
//            }
//            recordInputAndOutput(task)
        }

        void recordInputAndOutput(Task task) {
            if (task.name == "lintVitalRelease") {
                return
            }
            println("task_name[${task.name} --- task_class[${task.class.name}]\n")
            ArrayList<String> record = new ArrayList<>()
            task.inputs.files.files.each {
                record.add("[input_path]:[${it.absolutePath}]")
            }
            task.outputs.files.files.each {
                record.add("[output_path]:[${it.absolutePath}]")
            }
            record.each {
                println(it)
            }
//            FileUtil.saveFile(mProject.getRootDir(), "allTaskInputAndOutput",
//                    {
//                        return record
//                    })
        }
    }

}