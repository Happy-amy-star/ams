package net.geekhour.loki.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geekhour.loki.common.ResponseUtil;
import net.geekhour.loki.entity.dto.AssetDTO;
import net.geekhour.loki.entity.dto.AssetSummaryDTO;
import net.geekhour.loki.service.IAssetService;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import net.geekhour.loki.entity.dto.AssetImportResultDTO;
/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Jasper Yang
 * @since 2025-04-05
 */
@CrossOrigin
@RestController
@RequestMapping("/api/asset")
public class AssetController {

    @Autowired
    IAssetService assetService;
    private final ObjectMapper objectMapper = new ObjectMapper();  // ← 新增

    /**
     * list all assets (列出所有资产)
     *
     * @return Asset
     */
    @RequestMapping("/all")
    @PreAuthorize("hasRole('USER') || hasAuthority('user:asset:list')")
    public ResponseEntity<?> all() {
        return ResponseUtil.success(assetService.all());
    }

    /**
     * Get the total count of assets (获取资产总数)
     *
     * @return ResponseEntity
     */
    @PostMapping("/count")
    @PreAuthorize("hasRole('USER') || hasAuthority('user:asset:count')")
    public ResponseEntity<?> getAssetCount() {
        try {
            Long total = assetService.countAssets(null);
            return ResponseUtil.success(Map.of("total", total));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.error(500, "获取资产数量失败: " + e.getMessage());
        }
    }

    /**
     * 获取资产总数量和总金额
     *
     * @return ResponseEntity
     */
    @PostMapping("/summary")
    @PreAuthorize("hasRole('USER') || hasAuthority('user:asset:summary')")
    public ResponseEntity<?> getAssetSummary() {
        try {
            AssetSummaryDTO assetSummaryDTO = assetService.getSummary();
            return ResponseUtil.success(assetSummaryDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.error(500, "获取资产汇总信息失败: " + e.getMessage());
        }
    }

    /**
     * list all assets (列出所有资产)
     *
     * @param requestBody {name: "资产名称", pageIndex: 1, pageSize: 10}
     * @return AssetDTO
     */
    @CrossOrigin
    @RequestMapping("/list")
    @PreAuthorize("hasRole('USER') || hasAuthority('user:asset:list')")
    public ResponseEntity<?> getAssetList(@RequestBody(required = false) String requestBody) {
        String name = null;
        Integer pageIndex = 1;
        Integer pageSize = 10;
        if (requestBody != null && !requestBody.isEmpty()) {
            try {
                Map<String, Object> requestMap = new ObjectMapper().readValue(requestBody,
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                        });
                name = (String) requestMap.get("name");
                Object pageIndexObj = requestMap.get("pageIndex");
                Object pageSizeObj = requestMap.get("pageSize");
                pageIndex = pageIndexObj == null ? 1 : ((Number) pageIndexObj).intValue();
                pageSize = pageSizeObj == null ? 10 : ((Number) pageSizeObj).intValue();
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseUtil.error(400, e.getMessage());
            }
        }
        Integer offset = (pageIndex - 1) * pageSize;
        List<AssetDTO> assetList = assetService.getAssetList(offset, pageSize, name);
        Long total = assetService.countAssets(name);
        return ResponseUtil.success(Map.of(
                "items", assetList,
                "total", total));
    }

    /**
     * Delete an asset by ID
     *
     * @param id Asset ID
     * @return ResponseEntity
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('USER') || hasAuthority('user:asset:delete')")
    public ResponseEntity<?> deleteAsset(@PathVariable Long id) {
        return assetService.deleteAsset(id)
                ? ResponseUtil.success(id)
                : ResponseUtil.error(404, "资产不存在");
    }

    /**
     * Create a new asset
     *
     * @param assetDTO Asset details
     * @return ResponseEntity
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER') || hasAuthority('user:asset:create')")
    public ResponseEntity<?> createAsset(@RequestBody AssetDTO assetDTO) {
        if (assetDTO.getName() == null || assetDTO.getName().isEmpty()) {
            return ResponseUtil.error(400, "资产名称不能为空");
        }
        if (assetDTO.getCode() == null || assetDTO.getCode().isEmpty()) {
            return ResponseUtil.error(400, "资产编码不能为空");
        }
        if (assetService.checkAssetCodeExists(assetDTO.getCode())) {
            return ResponseUtil.error(400, "资产编码已存在");
        }
        if (assetService.checkAssetSnExists(assetDTO.getSn())) {
            return ResponseUtil.error(400, "资产序列号已存在");
        }
        try {
            return assetService.createAsset(assetDTO)
                    ? ResponseUtil.success(assetDTO)
                    : ResponseUtil.error(500, "资产创建失败");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.error(500, e.getMessage());
        }
    }

    /**
     * Update an existing asset
     *
     * @param assetDTO Updated asset details
     * @return ResponseEntity
     */
    @PostMapping("/update")
    @PreAuthorize("hasRole('USER') || hasAuthority('user:asset:update')")
    public ResponseEntity<?> updateAsset(@RequestBody AssetDTO assetDTO) {
        System.out.println("【Asset】 controller 【update】 method called ...");
        if (assetDTO.getId() == null) {
            return ResponseUtil.error(400, "资产ID不能为空");
        }
        if (assetDTO.getName() == null || assetDTO.getName().isEmpty()) {
            return ResponseUtil.error(400, "资产名称不能为空");
        }
        if (assetDTO.getCode() == null || assetDTO.getCode().isEmpty()) {
            return ResponseUtil.error(400, "资产编码不能为空");
        }
        try {
            return assetService.updateAsset(assetDTO)
                    ? ResponseUtil.success(assetDTO)
                    : ResponseUtil.error(500, "更新资产失败");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.error(500, e.getMessage());
        }
    }

    /** —— 导出：一次性导出页面所有资产 —— */

//    public ResponseEntity<ByteArrayResource> exportToExcel(@RequestBody(required = false) String requestBody) {
//        try {
//            // 1. 解析前端 JSON 参数
//            Map<String, Object> params = objectMapper.readValue(
//                    requestBody == null ? "{}" : requestBody,
//                    Map.class
//            );
//
//            // 2. 让 Service 生成一个 Workbook（不再写入 response）
//            Workbook workbook = assetService.buildExportWorkbook(params);
//
//            // 3. 把 Workbook 写到内存里
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            workbook.write(baos);
//            workbook.close();
//            byte[] bytes = baos.toByteArray();
//
//            // 4. 构建 HTTP 头
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(
//                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
//            );
//            headers.setContentDisposition(
//                    ContentDisposition.attachment().filename("assets.xlsx").build()
//            );
//            headers.setContentLength(bytes.length);
//
//            // 5. 包装成 ByteArrayResource 并返回
//            ByteArrayResource resource = new ByteArrayResource(bytes);
//            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
//
//        } catch (Exception ex) {
//            throw new RuntimeException("导出资产失败: " + ex.getMessage(), ex);
//        }
//    }

    /**
     * —— 导出：一次性导出页面所有资产 ——
     */
    @PostMapping("/export")
    @PreAuthorize("hasRole('USER') || hasAuthority('user:asset:export')")
    public ResponseEntity<ByteArrayResource> exportToExcel(
            @RequestBody(required = false) String requestBody) {
        try {
            // 1. 解析前端 JSON 参数（直接用 Map.class 就行了）
            Map<String, Object> params = objectMapper.readValue(
                    requestBody == null ? "{}" : requestBody,
                    Map.class
            );

            // 2. 用 Service 里统一的 buildExportWorkbook 生成 Workbook
            Workbook workbook = assetService.buildExportWorkbook(params);

            // 3. 写入到内存
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            workbook.close();
            byte[] bytes = baos.toByteArray();

            // 4. 构建响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                    MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )
            );
            headers.setContentDisposition(
                    ContentDisposition
                            .attachment()
                            .filename("assets.xlsx")
                            .build()
            );
            headers.setContentLength(bytes.length);

            // 5. 直接把字节数组包装成 Resource，返回给前端
            ByteArrayResource resource = new ByteArrayResource(bytes);
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);

        } catch (Exception ex) {
//            throw new RuntimeException("导出资产失败: " + ex.getMessage(), ex);
            // 捕获所有异常，返回错误提示的 Blob
            String errorMessage = "导出失败: " + ex.getMessage();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "text/plain")
                    .body(new ByteArrayResource(errorMessage.getBytes()));
        }
    }
    /** —— 导入：上传 Excel 文件并批量入库 —— */
    @PostMapping("/import")
    @PreAuthorize("hasRole('USER') || hasAuthority('user:asset:import')")
    public ResponseEntity<?> importFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            // 调用 assetService 的 importFromExcel 方法，将上传的 Excel 文件批量入库
            AssetImportResultDTO result = assetService.importFromExcel(file);
            return ResponseUtil.success(result);
        } catch (Exception e) {
            return ResponseUtil.error(500, "导入失败: " + e.getMessage());
            // 返回错误结果
        }
    }
}
