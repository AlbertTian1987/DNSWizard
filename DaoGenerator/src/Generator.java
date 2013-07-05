import de.greenrobot.daogenerator.*;

/**
 * Created with IntelliJ IDEA.
 * User: emellend
 * Date: 13-7-5
 * Time: 下午3:43
 * To change this template use File | Settings | File Templates.
 */
public class Generator {

    public static void main(String[] args) {
        Schema schema=new Schema(1,"com.tgh.dnswizard.bean");
        addEntity(schema);
        try {
            new DaoGenerator().generateAll(schema,"src");
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static void addEntity(Schema schema){
        //ISP
        Entity isp = schema.addEntity("ISP");
        isp.addIdProperty().autoincrement();
        isp.addStringProperty("text").notNull();

        //Province
        Entity province = schema.addEntity("Province");
        province.addIdProperty().autoincrement();
        Property text = province.addStringProperty("text").notNull().getProperty();
        //省只有一个运营商
        Property ispId = province.addLongProperty("ispId").getProperty();
        province.addToOne(isp,ispId);
        //运营商包含多个省
        ToMany ispToProvinces = isp.addToMany(province, ispId);
        ispToProvinces.setName("provinces");
        ispToProvinces.orderAsc(text);

        //city
        Entity city = schema.addEntity("City");
        city.addIdProperty().autoincrement();
        text = city.addStringProperty("text").notNull().getProperty();
        //市只对应一个省
        Property provinceId = city.addLongProperty("provinceId").notNull().getProperty();
        city.addToOne(province,provinceId);
        //省包含了多个市
        ToMany provinceToCities = province.addToMany(city, provinceId);
        provinceToCities.setName("cities");
        provinceToCities.orderAsc(text);

        //DNS
        Entity dns = schema.addEntity("DNS");
        dns.addIdProperty().autoincrement();
        text=dns.addStringProperty("text").notNull().getProperty();
        //DNS只对应一个市
        Property cityId = dns.addLongProperty("cityId").notNull().getProperty();
        dns.addToOne(city,cityId);
        //市包含多个DNS
        ToMany cityToDNS = city.addToMany(dns, cityId);
        cityToDNS.setName("dnsList");
        cityToDNS.orderAsc(text);
    }
}
