package net.geekhour.loki.service;

import net.geekhour.loki.entity.Asset;
import com.baomidou.mybatisplus.extension.service.IService;
import net.geekhour.loki.entity.dto.AssetDTO;
import net.geekhour.loki.entity.dto.AssetSummaryDTO;
import org.springframework.http.ResponseEntity;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;

import net.geekhour.loki.entity.dto.AssetImportResultDTO;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.apache.poi.ss.usermodel.Workbook;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Jasper Yang
 * @since 2025-04-05
 */
public interface IAssetService extends IService<Asset> {

    List<Asset> all();

    List<AssetDTO> getAssetList(Integer offset, Integer size, String name);

    Long countAssets(String name);

    boolean deleteAsset(Long id);

    boolean createAsset(AssetDTO assetDTO);

    boolean checkAssetCodeExists(String assetCode);

    boolean checkAssetNameExists(String assetName);

    boolean updateAsset(AssetDTO assetDTO);

    boolean checkAssetSnExists(String sn);

    BigDecimal calculateTotalAmount();

    AssetSummaryDTO getSummary();

    /** 导出符合条件的资产到 Excel */
    void exportToExcel(Map<String, Object> params, HttpServletResponse response) throws IOException;


    /** 从上传的 Excel 解析并批量保存资产，返回导入结果 */
    AssetImportResultDTO importFromExcel(MultipartFile file) throws IOException;

    Workbook buildExportWorkbook(Map<String, Object> params);

}
