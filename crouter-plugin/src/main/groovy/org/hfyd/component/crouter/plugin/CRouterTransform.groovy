package org.hfyd.component.crouter.plugin


import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager

class CRouterTransform extends Transform {

    @Override
    String getName() {
        return "CRouterAutoRegister"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        beforeTransform()
        execTransform(transformInvocation)
        afterTransform()
    }

    void beforeTransform() {
        CLog.i("-------------------------------------------------------")
        CLog.i("----------------CRouterAutoRegister Start----------------")
        CLog.i("-------------------------------------------------------")

    }

    private void execTransform(TransformInvocation transformInvocation)
            throws IOException, TransformException, InterruptedException {
        long startTime = System.currentTimeMillis()
        if (!transformInvocation.incremental) {
            transformInvocation.outputProvider.deleteAll()
        }

        TransformHelper helper = new TransformHelper(transformInvocation)
        helper.transform(new ScanClassModify())
        helper.transform(new InsertClassModify())

        CLog.i("此次编译共耗时:${System.currentTimeMillis() - startTime}毫秒")
    }


    void afterTransform() {

    }
}