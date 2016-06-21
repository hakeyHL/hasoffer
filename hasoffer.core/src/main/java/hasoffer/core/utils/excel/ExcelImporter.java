package hasoffer.core.utils.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import hasoffer.core.persistence.dbm.HibernateDao;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ExcelImporter {

	@Resource
	private JdbcTemplate jdbcTemplate;

	@Resource
	private HibernateDao dao;

	private ImportConfig importConfig;

	private ImportConfig getImportConfig() {
		return importConfig;
	}

	public ExcelImporter setImportConfig(ImportConfig importConfig) {
		this.importConfig = importConfig;
		return this;
	}

	public void importExcelFile(MultipartFile multipartFile, String realPath) throws IOException {
		File file = this.transferLocal(multipartFile, realPath);
		ImportConfig importConfig = this.getImportConfig();
		if (importConfig == null) throw new RuntimeException("无参数配置，无法执行导入操作");
		// 根据文件后缀名创建不同版本的excel
		Workbook xwb = file.getName().contains("xlsx") ? new XSSFWorkbook(new FileInputStream(file)) : new HSSFWorkbook(new FileInputStream(file));
		// 进行excel模版校验
		String error = importConfig.validation(xwb);
		if (null != error && !"".equals(error)) throw new RuntimeException(error);
		// 获取表格中的数据，按行形成List<Object[]>
		List<Object[]> preExecution = new LinkedList<Object[]>();
		Sheet sheet = xwb.getSheetAt(0);
		//用第一行的列数当做总的数据列数
		int cells = sheet.getRow(0).getLastCellNum();
		for (int i = 1, length = sheet.getLastRowNum(); i <= length; i++) {
			Row row = sheet.getRow(i);
			Object[] tempData = new Object[cells];
			//判断是否为空，如果为空则不处理
			if (StringUtils.isNotEmpty(getCellFormatValue(row.getCell(0)).trim())) {
				for (int j = 0; j < cells; j++) {
					tempData[j] = getCellFormatValue(row.getCell(j));
				}
				preExecution.add(tempData);
			}
		}
		// 导入之前的一些必要操作
		importConfig.getImportCallBack().preOperation(dao, preExecution);
		// 处理Excel中的数据
		final List<Object[]> batchArgs = importConfig.getImportData(dao, preExecution);
		// 执行导入过程
		int[] success = jdbcTemplate.batchUpdate(importConfig.getImportSQL(), new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
				Object[] objects = batchArgs.get(i);
				for (int j = 0, length = objects.length; j < length; j++)
					preparedStatement.setObject(j + 1, objects[j]);
			}

			@Override
			public int getBatchSize() {
				return batchArgs.size();
			}
		});
		// 导入成功，执行导入之后的操作
		if (success.length > 0) {
			importConfig.getImportCallBack().postOperation(dao, batchArgs);
		}
	}

    private File transferLocal(MultipartFile multipartFile, String realPath) throws IOException {
        String today = org.apache.http.client.utils.DateUtils.formatDate(new Date(), "yyyyMMddHHmmss");
        File dir = new File(realPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //文件命名方式为年月日时分秒+后缀名
        File file = new File(dir, today + multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf(".")));
        multipartFile.transferTo(file);
        return file;
    }

	private String getCellFormatValue(Cell cell) {
		String cellvalue = "";
		if (cell != null) {
			switch (cell.getCellType()) {
				case HSSFCell.CELL_TYPE_NUMERIC: {
					if (HSSFDateUtil.isCellDateFormatted(cell)) {
						Date date = cell.getDateCellValue();
						cellvalue = org.apache.http.client.utils.DateUtils.formatDate(date, "yyyy-MM-dd");
					} else {
						cellvalue = String.valueOf(cell.getNumericCellValue());
					}
					break;
				}
				case HSSFCell.CELL_TYPE_FORMULA: {
					if (HSSFDateUtil.isCellDateFormatted(cell)) {
						Date date = cell.getDateCellValue();
						cellvalue = org.apache.http.client.utils.DateUtils.formatDate(date, "yyyy-MM-dd");
					} else {
						cellvalue = String.valueOf(cell.getNumericCellValue());
					}
					break;
				}
				case HSSFCell.CELL_TYPE_STRING:
					cellvalue = cell.getRichStringCellValue().getString();
					break;
				default:
					cellvalue = " ";
			}
		} else {
			cellvalue = "";
		}
		return cellvalue.trim();
	}

}
