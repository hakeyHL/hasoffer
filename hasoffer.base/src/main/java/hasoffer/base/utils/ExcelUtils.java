package hasoffer.base.utils;

import com.alibaba.fastjson.util.IOUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

public class ExcelUtils {
	public static List<Map<String, String>> readRows(File file) throws IOException {
		return readRows(0, file);
	}

	public static List<Map<String, String>> readRows(int startRowIndex, File file) throws IOException {
		List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
		String fileName = file.getName();
		String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
		if (suffix.equals("xls") || suffix.equals("xlsx")) {
			rows = readRowsFromExcel(startRowIndex, file);
		}
		if (suffix.equals("csv")) {
			// todo 读csv文件有bug
			rows = readRowsFromCSV(startRowIndex, file);
		}
		return rows;
	}


	private static List<Map<String, String>> readRowsFromExcel(int startRowIndex, File file) throws IOException {
		InputStream in = new FileInputStream(file);
		List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
		Workbook wb;
		try {
			wb = new HSSFWorkbook(in);
		} catch (Exception e) {
			in = new FileInputStream(file);
			wb = new XSSFWorkbook(in);
		}
		Sheet sheet1 = wb.getSheetAt(0);
		for (Row row : sheet1) {
			int rowIndex = row.getRowNum();
			if (rowIndex < startRowIndex) {
				continue;
			}
			rows.add(readRow(row));
		}
		IOUtils.close(in);
		return rows;
	}


	public static List<Map<String, String>> readRowsFromCSV(int startRowIndex, File file) throws IOException {
		List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
		Charset charSet = Charset.forName("UTF-8");
		CSVParser parser = CSVParser.parse(file, charSet, CSVFormat.EXCEL);
		List<CSVRecord> recordList = parser.getRecords();

		for (int i = 0, size = recordList.size(); i < size; i++) {
			if (i < startRowIndex) {
				continue;
			}
			rows.add(readRow(recordList.get(i)));
		}
		return rows;
	}

	private static Map<String, String> readRow(Row row) {
		Map<String, String> map = new HashMap<String, String>();
		if (row != null) {
			int rowNumber = row.getRowNum();
			map.put("rowNumber", String.valueOf(rowNumber));
			for (short index = row.getFirstCellNum(); index < row.getLastCellNum(); index++) {
				map.put(String.valueOf(index), readCellValue(row.getCell(index)));
			}
		}
		return map;
	}

	private static String readCellValue(Cell cell) {
		if (cell == null) {
			return "";
		}
		int type = cell.getCellType();
		switch (type) {
			case Cell.CELL_TYPE_BLANK:
				return "";
			case Cell.CELL_TYPE_BOOLEAN:
				return String.valueOf(cell.getBooleanCellValue());
			case Cell.CELL_TYPE_ERROR:
				return String.valueOf(cell.getErrorCellValue());
			case Cell.CELL_TYPE_FORMULA:
				return cell.getCellFormula();
			case Cell.CELL_TYPE_NUMERIC:
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				return String.valueOf(cell.getStringCellValue());
			case Cell.CELL_TYPE_STRING:
				return cell.getStringCellValue();
			default:
				return "";
		}
	}

	private static Map<String, String> readRow(CSVRecord record) {
		if (record != null) {
			return record.toMap();
		}
		return Collections.emptyMap();
	}

	public static void main(String[] args) {
		try {
			File file = new File("D:\\tmp\\mobiles.xlsx");
			List<Map<String, String>> records = readRows(0, file);

			System.out.print(records.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
