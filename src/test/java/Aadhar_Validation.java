import com.jayway.jsonpath.JsonPath;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.restassured.RestAssured.given;

public class Aadhar_Validation {


    public static Properties prop;

    Connection dbconnection;






    void create_table()
// TO create a tabel in the DB.
    {
        try
        {

            //URL, DB User Name , DB Password
            dbconnection= DriverManager.getConnection(prop.getProperty("url"), prop.getProperty("user"),prop.getProperty("password"));
            if (dbconnection != null)
            {
                System.out.println("Database server is connected");
            }

            Statement stamnt = dbconnection.createStatement();
            stamnt.execute("use capstone");


            // Create an empty Table

            stamnt.execute("create table new_capstone\n" +
                    "(Fname varchar(20) ,\n" +
                    "Lname varchar(20) ,\n" +
                    "Aadhar_No varchar(50) , \n" +
                    "Address varchar(50) , \n" +
                    "phone_no  varchar(10) ) ");


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }


    void insertintotable()
            //TO insert the records in the table.
    {
        try
        {

            //URL, DB User Name , DB Password
            dbconnection= DriverManager.getConnection(prop.getProperty("url"), prop.getProperty("user"),prop.getProperty("password"));
            if (dbconnection != null)
            {
                System.out.println("Database server is connected");
            }


            Statement stamnt = dbconnection.createStatement();
            // Use Database where we need to create a new Table

            stamnt.execute("use capstone");



            stamnt.execute("Insert into new_capstone(Fname,Lname,Aadhar_No,Address,phone_no) values " +
                    "('Shashi','Kapoor','978645429016','Bangalore','8794634725') ,\n" +
                    "('Rohit','Anand','569656356352','Mumbai','7653221197'),\n" +
                    "('Nancy','Priya','675736563563','Durgapur','5646342300'),\n" +
                    "('Shipra','Verma','865335657536','Dhanbad','7564624534');");



            System.out.println("Record Inserted");






        }
        catch (Exception e)
        {
            System.out.println(e);


        }

    }



    Map<String,String> select_datafromtable()
    {
        //For retriving the data from the DB.
         Map<String,String> DbInfo = new HashMap<String,String>();
        try
        {

            //URL, DB User Name , DB Password
            dbconnection= DriverManager.getConnection(prop.getProperty("url"), prop.getProperty("user"),prop.getProperty("password"));
            if (dbconnection != null)
            {
                System.out.println("Database server is connected");
            }


            Statement stamnt = dbconnection.createStatement();
            // Use Database where we need to create a new Table

            stamnt.execute("use capstone");

            ResultSet result= stamnt.executeQuery("SELECT Fname,Lname,Aadhar_No,Address,phone_no FROM capstone.new_capstone\n" +
                    "where Aadhar_No=\""+prop.getProperty("aadhar")+"\";");


            String Getfname = null;
            String Getaadharnumber= null;
            String Getrunaddress = null;
            String Getlname = null;
            String GetPhone= null;

            while (result.next())
            {
                Getaadharnumber =result.getString("Aadhar_No");
                Getrunaddress =result.getString("Address");
                Getfname =result.getString("Fname");
                Getlname =result.getString("Lname");
                GetPhone =result.getString("phone_no");
            }

            DbInfo.put("Fname",Getfname);
            DbInfo.put("Lname",Getlname);
            DbInfo.put("Phone",GetPhone);
            DbInfo.put("Aadhar_No",Getaadharnumber);
            DbInfo.put("Address",Getrunaddress);



        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
 return DbInfo;
    }


     Map<String,String> createUserinAPIusingproperiesfile(Map<String,String> dbresponse)
    {
        //Getting the data from and formatting it in JSON format and sending it to API using POST call.

        Map<String,String> ApiResponse = new HashMap<String,String>();
        Response res = given()
                .contentType(ContentType.JSON)
                .body("{\n" +
                        "    \"Fname\": \""+dbresponse.get("Fname")+"\",\n" +
                        "    \"Lname\": \""+dbresponse.get("Lname")+"\",\n" +
                        "    \"Aadhar_No\": \""+prop.getProperty("aadhar")+"\",\n" +
                        "    \"Address\": \""+dbresponse.get("Address")+"\",\n" +
                        "    \"Phone\": \""+dbresponse.get("Phone")+"\" \n" +

                        "}")

                .when()
                .post("https://reqres.in/api/users");

if(res.getStatusCode()==201) {
    String abc = res.getBody().toString();
    Assert.assertNotNull(abc.contains("id"));
    String id = JsonPath.read(res.getBody().asString(),"$.id");
    assert Integer.valueOf(id)>=0 ;
    System.out.println("id validated as Integer");
    String created_at_api = JsonPath.read(res.getBody().asString(),"$.createdAt");
    String createDate =created_at_api.substring(8,10);

    Date date = new Date();
    SimpleDateFormat sf = new SimpleDateFormat("YYYY-MM-dd");
    String current_date = sf.format(date);
    String currentdate  = current_date.substring(8,current_date.length());
    Assert.assertEquals(currentdate,createDate);
    System.out.println("Created Date is validated with  Current date-->"+currentdate+" Create date-->"+createDate);

    ApiResponse.put("Fname", JsonPath.read(res.getBody().asString(), "$.Fname"));
    ApiResponse.put("Lname", JsonPath.read(res.getBody().asString(), "$.Lname"));
    ApiResponse.put("Phone", JsonPath.read(res.getBody().asString(), "$.Phone"));
    ApiResponse.put("Aadhar_No", JsonPath.read(res.getBody().asString(), "$.Aadhar_No"));
    ApiResponse.put("Address", JsonPath.read(res.getBody().asString(), "$.Address"));
}
        return ApiResponse;
    }
    public  Aadhar_Validation(){
        // To call the values from the Proprties files using constructor.
        try {
            prop = new Properties();
            FileInputStream ip = new FileInputStream(System.getProperty("user.dir")+ "\\src\\test\\Application.properties");
            prop.load(ip);
        }  catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        //Main Method for executing all the methods by calling the objects.
        Map<String, String> APiRes = null;
        Aadhar_Validation adh = new Aadhar_Validation();
        adh.create_table();
        adh.insertintotable();
     Map<String,String> DbRes =   adh.select_datafromtable();
     if(DbRes!=null) {
          APiRes = adh.createUserinAPIusingproperiesfile(DbRes);
     }

        if(!DbRes.isEmpty() && !APiRes.isEmpty()){
          boolean validation =  DbRes.equals(APiRes);
            Assert.assertEquals(true,validation);
            System.out.println("Validation of API response and Db was successfull");

        }

    }
}
