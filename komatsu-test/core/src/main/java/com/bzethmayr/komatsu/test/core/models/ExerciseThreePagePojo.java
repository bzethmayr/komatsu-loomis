package com.bzethmayr.komatsu.test.core.models;

import java.util.Date;

public class ExerciseThreePagePojo {
    private final String title;
    private final String description;
    private final ImagePojo image;
    private final Date lastModified;

    public ExerciseThreePagePojo(
            final String title, final String description, final ImagePojo image, final Date lastModified
    ) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.lastModified = lastModified;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public ImagePojo getImage() {
        return image;
    }

    public Date getLastModified() {
        return lastModified;
    }
}
