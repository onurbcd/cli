package com.onurbcd.eruservice.model;

import com.onurbcd.eruservice.persistency.entity.Balance;
import com.onurbcd.eruservice.persistency.entity.Document;
import lombok.*;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CreateBalance {

    private Balance balance;
    private Set<Document> deleteDocuments;
}
