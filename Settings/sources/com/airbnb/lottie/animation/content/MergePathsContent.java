package com.airbnb.lottie.animation.content;

import android.annotation.TargetApi;
import android.graphics.Path;
import android.graphics.Path.Op;
import android.os.Build.VERSION;
import com.airbnb.lottie.model.content.MergePaths;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

@TargetApi(19)
public class MergePathsContent implements PathContent, GreedyContent {
    private final Path firstPath = new Path();
    private final MergePaths mergePaths;
    private final String name;
    private final Path path = new Path();
    private final List<PathContent> pathContents = new ArrayList();
    private final Path remainderPath = new Path();

    public MergePathsContent(MergePaths mergePaths) {
        if (VERSION.SDK_INT >= 19) {
            this.name = mergePaths.getName();
            this.mergePaths = mergePaths;
            return;
        }
        throw new IllegalStateException("Merge paths are not supported pre-KitKat.");
    }

    public void absorbContent(ListIterator<Content> contents) {
        while (contents.hasPrevious() && contents.previous() != this) {
        }
        while (contents.hasPrevious()) {
            Content content = (Content) contents.previous();
            if (content instanceof PathContent) {
                this.pathContents.add((PathContent) content);
                contents.remove();
            }
        }
    }

    public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
        for (int i = 0; i < this.pathContents.size(); i++) {
            ((PathContent) this.pathContents.get(i)).setContents(contentsBefore, contentsAfter);
        }
    }

    public Path getPath() {
        this.path.reset();
        switch (this.mergePaths.getMode()) {
            case Merge:
                addPaths();
                break;
            case Add:
                opFirstPathWithRest(Op.UNION);
                break;
            case Subtract:
                opFirstPathWithRest(Op.REVERSE_DIFFERENCE);
                break;
            case Intersect:
                opFirstPathWithRest(Op.INTERSECT);
                break;
            case ExcludeIntersections:
                opFirstPathWithRest(Op.XOR);
                break;
        }
        return this.path;
    }

    public String getName() {
        return this.name;
    }

    private void addPaths() {
        for (int i = 0; i < this.pathContents.size(); i++) {
            this.path.addPath(((PathContent) this.pathContents.get(i)).getPath());
        }
    }

    @TargetApi(19)
    private void opFirstPathWithRest(Op op) {
        this.remainderPath.reset();
        this.firstPath.reset();
        for (int i = this.pathContents.size() - 1; i >= 1; i--) {
            PathContent content = (PathContent) this.pathContents.get(i);
            if (content instanceof ContentGroup) {
                List<PathContent> pathList = ((ContentGroup) content).getPathList();
                for (int j = pathList.size() - 1; j >= 0; j--) {
                    Path path = ((PathContent) pathList.get(j)).getPath();
                    path.transform(((ContentGroup) content).getTransformationMatrix());
                    this.remainderPath.addPath(path);
                }
            } else {
                this.remainderPath.addPath(content.getPath());
            }
        }
        int j2 = 0;
        PathContent lastContent = (PathContent) this.pathContents.get(0);
        if (lastContent instanceof ContentGroup) {
            List<PathContent> pathList2 = ((ContentGroup) lastContent).getPathList();
            while (j2 < pathList2.size()) {
                Path path2 = ((PathContent) pathList2.get(j2)).getPath();
                path2.transform(((ContentGroup) lastContent).getTransformationMatrix());
                this.firstPath.addPath(path2);
                j2++;
            }
        } else {
            this.firstPath.set(lastContent.getPath());
        }
        this.path.op(this.firstPath, this.remainderPath, op);
    }
}
