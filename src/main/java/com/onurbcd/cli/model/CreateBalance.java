package com.onurbcd.cli.model;

import com.onurbcd.cli.persistency.entity.Balance;
import com.onurbcd.cli.persistency.entity.Document;
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
