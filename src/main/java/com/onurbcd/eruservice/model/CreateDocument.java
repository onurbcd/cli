package com.onurbcd.eruservice.model;

import com.onurbcd.eruservice.persistency.entity.Document;
import com.onurbcd.eruservice.util.CollectionUtil;
import lombok.*;
import org.springframework.lang.Nullable;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
public class CreateDocument {

    private Set<Document> saveDocuments;

    @Getter
    private Set<Document> deleteDocuments;

    @Nullable
    public Set<Document> getSaveDocuments() {
        return CollectionUtil.isEmpty(saveDocuments) ? null : saveDocuments;
    }
}
