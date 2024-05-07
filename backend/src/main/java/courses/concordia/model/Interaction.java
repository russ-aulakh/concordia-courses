package courses.concordia.model;

import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Document(collection = "interactions")
public class Interaction {
    @Id
    private String _id;
    private InteractionKind kind;
    private String type;
    private String courseId;
    private String instructorId;
    private String userId;
    private String referrer;

    @Getter
    @AllArgsConstructor
    public enum InteractionKind {
        LIKE("like"),
        DISLIKE("dislike");
        private final String value;
        @JsonValue
        public String toValue() {
            return this.value;
        }
        public static InteractionKind fromValue(String value) {
            InteractionKind ret = null;
            for (InteractionKind type : InteractionKind.values()) {
                if (type.getValue().equals(value)) ret = type;
            }
            return ret;
        }
    }
}

