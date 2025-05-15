import { AxiosRequestConfig } from "axios";
import loki from "./loki";
import {
  Asset,
  AssetDTO,
  AssetListResponse,
  AssetNamesResponse,
  AssetTypeListResponse,
  AssetSummaryResponse,
  AssetImportResultDTO,   // 在 types/asset.ts 新增的
} from "~/types/asset";


// 获取资产列表（支持分页和按名称查询）
export const getAssetList = async (
  params?: { pageIndex?: number; pageSize?: number; name?: string }
): Promise<AssetListResponse> => {

  return await loki.request({
    url: "/asset/list",
    method: "POST",
    data: params || {}, // 如果没有参数，传递空对象
  } as AxiosRequestConfig);

};
// 添加资产
export const addAsset = async (
  asset: AssetDTO
): Promise<AssetListResponse> => {
  return await loki.request({
    url: "/asset/create",
    method: "POST",
    data: {
      ...asset,
    },
  } as AxiosRequestConfig);
};

// 更新资产信息
export const updateAsset = async (
  asset: Asset
): Promise<AssetListResponse> => {
  return await loki.request({
    url: "/asset/update",
    method: "POST",
    data: {
      ...asset,
    },
  } as AxiosRequestConfig);
};
// 按照资产ID删除资产
export const deleteAssetById = async (
  id: string
): Promise<AssetListResponse> => {
  return await loki.request({
    url: "/asset/delete/" + id,
    method: "DELETE",
  } as AxiosRequestConfig);
};
// 按照资产ID获取资产信息
export const getAssetById = async (
  id: string
): Promise<AssetListResponse> => {
  return await loki.request({
    url: "/asset/get",
    method: "POST",
    data: {
      id,
    },
  } as AxiosRequestConfig);
};


// 批量删除资产
export const deleteAssets = async (
  ids: string[]
): Promise<AssetListResponse> => {
  return await loki.request({
    url: "/asset/delete/batch",
    method: "POST",
    data: {
      ids,
    },
  } as AxiosRequestConfig);
};

// 批量更新资产
export const updateAssets = async (
  assets: Asset[]
): Promise<AssetListResponse> => {
  return await loki.request({
    url: "/asset/update/batch",
    method: "POST",
    data: {
      assets,
    },
  } as AxiosRequestConfig);
};

// 获取资产类型名
export const getAssetNames = async (): Promise<AssetNamesResponse> => {
  return await loki.request({
    url: "/at/names",
    method: "POST",
  } as AxiosRequestConfig);
};

// 获取资产类型列表
export const getAssetTypeList = async (): Promise<AssetTypeListResponse> => {
  return await loki.request({
    url: "/at/list",
    method: "POST",
  } as AxiosRequestConfig);
};

// 资产领用
export interface BorrowAssetParams {
  assetId: string
  borrowCount: number
  borrowDepartment: string
  borrower: string
  borrowDate: string
  expectedReturnDate: string
  reason: string
}

export const borrowAsset = (data: BorrowAssetParams) => {
  return loki.request({
    url: '/asset/borrow',
    method: 'post',
    data
  })
}

// 资产领用记录
export interface AssetBorrowRecord {
  id: string
  name: string
  code: string
  type: string
  borrowDepartment: string
  borrower: string
  borrowCount: number
  borrowDate: string
  expectedReturnDate: string
  actualReturnDate?: string
  status: 'using' | 'returned' | 'overdue'
  reason: string
  returnNote?: string
}

export interface AssetBorrowListResponse {
  items: AssetBorrowRecord[]
  total: number
}

// 获取资产领用记录
export const getAssetBorrowRecords = (params: {
  name?: string
  status?: string
  pageIndex: number
  pageSize: number
}) => {
  return loki.request<AssetBorrowListResponse>({
    url: '/asset/borrow/records',
    method: 'get',
    params
  })
}

// 归还资产
export const returnAsset = (data: {
  borrowId: string
  returnDate: string
  returnNote?: string
}) => {
  return loki.request<{ code: number; message: string }>({
    url: '/asset/return',
    method: 'post',
    data
  })
}

export const getAssetSummary = async (): Promise<AssetSummaryResponse> => {
  return await loki.request({
    url: '/asset/summary',
    method: 'post'
  } as AxiosRequestConfig);
}

/**
 * 导出资产列表为 Excel（二进制流）
 * @param params 同 getAssetList 的 { pageIndex?, pageSize?, name? }
 */
// export const exportAssetList = async (
//     params?: { pageIndex?: number; pageSize?: number; name?: string }
// ): Promise<Blob> => {
//   // 这里给 request 指定泛型，告诉 TS data 部分是 Blob
//   const res = await loki.request<Blob>({
//     url: "/asset/export",
//     method: "POST",
//     data: params || {},
//     responseType: "blob",
//   } as AxiosRequestConfig);
//
//   // res 是 { code, message, data: Blob }
//   return res.data;
// };
export const exportAssetList = async (
    params?: { pageIndex?: number; pageSize?: number; name?: string }
): Promise<Blob> => {
  try {
    const res = await loki.request<Blob>({
      url: "/asset/export",
      method: "POST",
      data: params || {},
      responseType: "blob",
    } as AxiosRequestConfig);
    console.log("导出响应数据:", res.data);  // ← 新增日志
    return res.data;
  } catch (error) {
    console.error("导出请求失败:", error);
    throw error;
  }
};


/**
 * 从 Excel 批量导入资产
 * @param file 用户选中的 .xls/.xlsx 文件
 */
export const importAssetList = async (
    file: File
): Promise<AssetImportResultDTO> => {
  const form = new FormData();
  form.append("file", file);
  return await loki.request({
    url: "/asset/import",
    method: "POST",
    data: form,
    headers: { "Content-Type": "multipart/form-data" },
  } as AxiosRequestConfig);
};
