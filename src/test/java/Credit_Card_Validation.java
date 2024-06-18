import com.jayway.jsonpath.JsonPath;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.io.FileInputStream;
import java.util.*;

import static io.restassured.RestAssured.given;


public class Credit_Card_Validation {



    public static void main(String args[]) {
        List creditCardNo =  read_Text_As_Per_Data_XL();
        Fetch_card_details(creditCardNo);
    }

    public static List read_Text_As_Per_Data_XL() {
//Read data from the excel and storing the response from in array list
        List<String> CCNo = new ArrayList();
        try {

            String XLFilePath = "C:/Users/karthikeyanra/Pictures/CreditCardDetails.xlsx";
            FileInputStream myxlfile = new FileInputStream(XLFilePath);
            Workbook workbook = new XSSFWorkbook(myxlfile);
            Sheet sheet = workbook.getSheet("Sheet1");
            int lastRow = sheet.getLastRowNum();
            System.out.println("The last row which has data ==" + lastRow);

            // Loop for Row Iteration...
            for (int i = 0; i <= lastRow; i++) {
                Row row = sheet.getRow(i);

                int lastCell = row.getLastCellNum();
                Cell cell = row.getCell(0);
                String runtimeTestCaseName = cell.getStringCellValue();

                Row RowNew = sheet.getRow(0);
                Cell cellnew = RowNew.getCell(0);
                String RunTimeCallValue = cellnew.getStringCellValue();

                String value = sheet.getRow(i).getCell(0).toString();
                CCNo.add(value);




            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CCNo;

    }

    public static void create_table() {
        //Create Table and inserting the data in to the table for customer details of the Credit card.
        try {

            //URL, DB User Name , DB Password
            Connection    dbconnection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306", "root", "Maveric@28");
            if (dbconnection != null) {
                System.out.println("Database server is connected");
            }

            Statement stamnt = dbconnection.createStatement();
            stamnt.execute("use capstone");


            // Create an empty Table

            stamnt.execute("create table Credit_card\n" +
                    "(Cust_Name varchar(20) ,\n" +
                    "Issued_Year varchar(20) ,\n" +
                    "Credit_Card_Number varchar(50) , \n" +
                    "Card_Limit varchar(20), \n" +
                    "EXP_Date varchar(20) , \n" +
                    "Card_Type  varchar(10) ) ");


            stamnt.execute("Insert into Credit_card(Cust_Name,Issued_Year,Credit_Card_Number,Card_Limit,EXP_Date,Card_Type) values " +
                    "('Shashi','2019','7673676497537640','30000','01-08-2024','VISA') ,\n" +
                    "('Rohit','2020','7673676497537641','20000','06-09-2026','MASTER'),\n" +
                    "('Nancy','2022','7673676497537642','35000','22-11-2027','RUPAY'),\n" +
                    "('Surbhi','2018','7673676497537643','50000','09-02-2023','VISA'),\n" +
                    "('Natasha','2019','7673676497537644','55000','07-09-2024','MASTER'),\n" +
                    "('Saurav','2016','7673676497537645','58000','14-06-2025','RUPAY'),\n" +
                    "('Amit','2014','7673676497537646','64000','23-04-2022','VISA'),\n" +
                    "('Raj','2015','7673676497537647','70000','12-05-2023','MASTER'),\n" +
                    "('Renu','2018','7673676497537648','77000','15-07-2024','RUPAY'),\n" +
                    "('Shipra','2016','7673676497537649','80000','24-01-2022','VISA');");

            stamnt.execute("create table Pan_card\n" +
                    "(Credit_Card_Number varchar(20) ,\n" +
                    "PAN_CARD varchar(20)  ) ");

//Creating a table and inserting the data of Pancard numbers associated with Creditcard numbers.

            stamnt.execute("Insert into Pan_card(Credit_Card_Number,PAN_CARD) values " +
                    "('7673676497537640','SHASI1244A') ,\n" +
                    "('7673676497537641','ROHIT1244B'),\n" +
                    "('7673676497537642','NANCY1244C'),\n" +
                    "('7673676497537643','SCCYU1244D'),\n" +
                    "('7673676497537644','NCCYU1244E'),\n" +
                    "('7673676497537645','SCCYU1244F'),\n" +
                    "('7673676497537646','ACCYU1244G'),\n" +
                    "('7673676497537647','RCCYU1244H'),\n" +
                    "('7673676497537648','RCCYU1244I'),\n" +
                    "('7673676497537649','SCCYU1244J');");

        } catch (Exception e) {
            e.printStackTrace();
        }


    }



    public static void Fetch_card_details(List<String> CCNo) {
   //Calling the methods for Creating table and inserting data and retrieving the CC data information and passing to CC API .
        if(CCNo!=null) {
            create_table();
            for (int i = 1; i < CCNo.size(); i++) {
                Map<String, String> Dbdata = select_datafromtable(CCNo.get(i));
                System.out.println(Dbdata+""+i);
                CreditcardAPI(Dbdata);
            }
        }
    }

    public static Map<String, String> select_datafromtable(String CCNo) {
        //Method to retrieve the DB information
        Map<String, String> DBInfo = new HashMap<>();
        try {

            //URL, DB User Name , DB Password
            Connection   dbconnection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306", "root", "Maveric@28");
            if (dbconnection != null) {
                System.out.println("Database server is connected");
            }


            Statement stamnt = dbconnection.createStatement();
            // Use Database where we need to create a new Table

            stamnt.execute("use capstone");

            ResultSet result = stamnt.executeQuery("SELECT Cust_Name,Issued_Year,Credit_Card_Number,Card_Limit,EXP_Date,Card_Type FROM capstone.Credit_card\n" +
                    "where Credit_Card_Number=\"" + CCNo + "\";");


            while (result.next()) {
                String Name = result.getString("Cust_Name");
                String Year = result.getString("Issued_Year");
                String CC_No = result.getString("Credit_Card_Number");
                String Limit = result.getString("Card_Limit");
                String Exp_date = result.getString("EXP_Date");
                String Card_type = result.getString("Card_Type");
                DBInfo.put("name", Name);
                DBInfo.put("year", Year);
                DBInfo.put("Credit Card Number", CC_No);
                DBInfo.put("Limit", Limit);
                DBInfo.put("EXP Date", Exp_date);
                DBInfo.put("Card Type", Card_type);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return DBInfo;
    }


    public static void CreditcardAPI(Map<String, String> DBInfo) {
        //Method for data loading in the API
        Map<String, String> APiData = new HashMap<String, String>();
        String name = DBInfo.get("name");
        String request_body = "{\n" +
                "\"name\": \""+ name +"\" ,\n" +
                "\"data\": {\n" +
                "\"year\": " + DBInfo.get("year") + ",\n" +
                "\"Credit Card Number\": \"" + DBInfo.get("Credit Card Number") + "\",\n" +
                "\"Limit\": \"" + DBInfo.get("Limit") + "\",\n" +
                "\"EXP Date\": \"" + DBInfo.get("EXP Date").toString() + "\",\n" +
                "\"Card Type\": \"" + DBInfo.get("Card Type").toString() + "\"\n" +
                "}\n" +
                "}";

        Response response = given().baseUri("https://api.restful-api.dev").header("Content-Type", "application/json")
                .body(request_body).when().post("/objects");
        System.out.println("Response"+response.asString());
        int statusCode = response.statusCode();
        if (statusCode == 200) {
            String res = response.getBody().asString();
            String api_CCName  =JsonPath.read(res.toString(),"$.name");
            Integer api_CCYear  =JsonPath.read(res,"$.data.year");
            String api_CCNO  =JsonPath.read(res.toString(),"$.data.['Credit Card Number']");
            String api_CCLimit =JsonPath.read(res.toString(),"$.data.Limit");
            String api_CCExp  =JsonPath.read(res.toString(),"$.data.['EXP Date']");
            String api_CCType  =JsonPath.read(res.toString(),"$.data.['Card Type']");
            String api_id  =JsonPath.read(res.toString(),"$.id");
            String api_Createddate  =JsonPath.read(res.toString(),"$.createdAt");


            APiData.put("name", api_CCName);
            APiData.put("year", String.valueOf(api_CCYear));
            APiData.put("Credit Card Number", api_CCNO);
            APiData.put("Limit", api_CCLimit);
            APiData.put("EXP Date", api_CCExp);
            APiData.put("Card Type", api_CCType);
            APiData.put("id", api_id);
            APiData.put("createdAt", api_Createddate);
        }

        if(!APiData.isEmpty() && !DBInfo.isEmpty()){
            Set<Map.Entry<String, String>> api =APiData.entrySet();
            for(Map.Entry<String,String>apiresponse:api){
                String apiKey =  apiresponse.getKey();
                String apires =  apiresponse.getValue();
                if(DBInfo.containsKey(apiKey)) {
                    String dbres = DBInfo.get(apiKey);
                    if (apires.equals(dbres)) {
                        System.out.println("Key->" + apiKey + " API response-->" + apires + "is matched with Db response-->" + dbres);
                    }
                    else
                        System.out.println("Key->" + apiKey + " API response-->" + apires + " is not matched with Db response-->" + dbres);
                }
                else
                    System.out.println(apiKey +" is not present in Data base ");


            }
            System.out.println("------------------");
        }
    }

}
