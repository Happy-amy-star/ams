package net.geekhour.loki.entity.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class AssetImportResultDTO {
    private int total;
    private int successCount;
    private int failureCount;
    private List<String> errors;  // 如 "第3行：编码重复"
}
