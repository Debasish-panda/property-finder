package com.propertyfinder.api.property;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import com.propertyfinder.api.auth.JwtService;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.apache.poi.ss.usermodel.*;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;

@Path("/api") @Produces(MediaType.APPLICATION_JSON)
public class PropertyResource {
  @Inject JwtService jwt;

  @GET @Path("/properties")
  public Map<String,Object> properties(@QueryParam("north") double north,@QueryParam("south") double south,@QueryParam("east") double east,@QueryParam("west") double west) {
    List<PropertyEntity> rows=PropertyEntity.find("latitude between ?1 and ?2 and longitude between ?3 and ?4 and status = 'available'",south,north,west,east).list();
    return Map.of("properties", rows.stream().map(this::dto).toList(), "total", rows.size(), "viewport", Map.of("north",north,"south",south,"east",east,"west",west));
  }

  @POST @Path("/broker/properties/import") @Consumes(MediaType.MULTIPART_FORM_DATA) @Transactional
  public Response importProperties(@RestForm("file") FileUpload upload, @HeaderParam("Authorization") String authorization) {
    JwtService.Claims claims=claims(authorization);
    if (claims==null || !"BROKER".equals(claims.role())) return error(Response.Status.FORBIDDEN, "Broker authentication required");
    if (upload==null || upload.uploadedFile()==null) return error(Response.Status.BAD_REQUEST, "A CSV or Excel file is required");
    String name=upload.fileName()==null?"":upload.fileName().toLowerCase(Locale.ROOT);
    if (!(name.endsWith(".csv") || name.endsWith(".xlsx") || name.endsWith(".xls"))) return error(Response.Status.BAD_REQUEST, "Only .csv, .xlsx, and .xls files are supported");
    try {
      List<ImportedRow> rows=name.endsWith(".csv")?parseCsv(upload.uploadedFile()):parseExcel(upload.uploadedFile());
      if (rows.isEmpty()) return error(Response.Status.BAD_REQUEST, "The file contains no property rows");
      List<String> errors=new ArrayList<>(); List<PropertyEntity> valid=new ArrayList<>();
      for (int i=0;i<rows.size();i++) { try { valid.add(toEntity(rows.get(i), claims.userId(), i+2)); } catch (IllegalArgumentException e) { errors.add(e.getMessage()); } }
      if (!errors.isEmpty()) return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message","Import rejected; no rows were saved", "errors", errors)).build();
      valid.forEach(entity -> entity.persist()); return Response.ok(Map.of("message","Properties imported successfully", "imported", valid.size())).build();
    } catch (IllegalArgumentException e) { return error(Response.Status.BAD_REQUEST,e.getMessage()); }
      catch (Exception e) { return error(Response.Status.BAD_REQUEST,"Could not read the uploaded file. Check its format and headers"); }
  }

  @GET @Path("/broker/dashboard") public Response dashboard(@HeaderParam("Authorization") String authorization) {
    JwtService.Claims claims=claims(authorization); if(claims==null || !"BROKER".equals(claims.role())) return error(Response.Status.FORBIDDEN,"Broker authentication required");
    List<PropertyEntity> rows=PropertyEntity.find("brokerId",claims.userId()).list(); long available=rows.stream().filter(p->"available".equals(p.status)).count();
    return Response.ok(Map.of("properties",rows.stream().map(this::dto).toList(),"summary",Map.of("totalListings",rows.size(),"availableListings",available,"rentedListings",rows.size()-available,"occupancyRate",rows.isEmpty()?0:((rows.size()-available)*100.0/rows.size()),"commissionEarned",rows.stream().mapToDouble(p->p.commissionEarned==null?0:p.commissionEarned.doubleValue()).sum(),"commissionMonths",0))).build();
  }

  private List<ImportedRow> parseCsv(java.nio.file.Path path) throws IOException {
    List<ImportedRow> result=new ArrayList<>(); try(BufferedReader reader=Files.newBufferedReader(path, StandardCharsets.UTF_8)) { String header=reader.readLine(); if(header==null) return result; List<String> headers=csvLine(header); String line; while((line=reader.readLine())!=null) { if(line.isBlank()) continue; List<String> values=csvLine(line); Map<String,String> row=new HashMap<>(); for(int i=0;i<headers.size();i++) row.put(norm(headers.get(i)), i<values.size()?values.get(i).trim():""); result.add(ImportedRow.from(row)); } } return result;
  }
  private List<ImportedRow> parseExcel(java.nio.file.Path path) throws IOException { List<ImportedRow> result=new ArrayList<>(); try(InputStream in=Files.newInputStream(path); Workbook book=WorkbookFactory.create(in)) { Sheet sheet=book.getSheetAt(0); if(sheet.getPhysicalNumberOfRows()<2) return result; Row header=sheet.getRow(sheet.getFirstRowNum()); Map<Integer,String> headers=new HashMap<>(); for(Cell c:header) headers.put(c.getColumnIndex(),norm(cell(c))); for(int r=header.getRowNum()+1;r<=sheet.getLastRowNum();r++) { Row source=sheet.getRow(r); if(source==null) continue; Map<String,String> values=new HashMap<>(); for(Map.Entry<Integer,String> h:headers.entrySet()) values.put(h.getValue(),cell(source.getCell(h.getKey()))); result.add(ImportedRow.from(values)); } } return result; }
  private List<String> csvLine(String line) { List<String> out=new ArrayList<>(); StringBuilder b=new StringBuilder(); boolean quoted=false; for(int i=0;i<line.length();i++){char c=line.charAt(i); if(c=='"'){if(quoted&&i+1<line.length()&&line.charAt(i+1)=='"'){b.append('"');i++;}else quoted=!quoted;}else if(c==','&&!quoted){out.add(b.toString());b.setLength(0);}else b.append(c);} out.add(b.toString()); return out; }
  private String cell(Cell cell) { if(cell==null)return ""; return new DataFormatter().formatCellValue(cell).trim(); }
  private String norm(String value) { return value==null?"":value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]",""); }

  private PropertyEntity toEntity(ImportedRow r,long brokerId,int rowNumber) { String address=required(r.address,"address/locality/location",rowNumber); BigDecimal rent=money(r.rent,"rent",rowNumber); if(r.latitude==null||r.longitude==null) throw new IllegalArgumentException("Row "+rowNumber+": latitude and longitude are required"); if(r.latitude<-90||r.latitude>90||r.longitude<-180||r.longitude>180) throw new IllegalArgumentException("Row "+rowNumber+": invalid latitude or longitude");
    PropertyEntity p=new PropertyEntity(); p.brokerId=brokerId;p.address=address;p.locality=address;p.title=blank(r.buildingName)?address:r.buildingName;p.buildingName=r.buildingName;p.houseNumber=r.houseNumber;p.ownerName=r.ownerName;p.ownerMobile=r.ownerMobile;p.rent=rent;p.deposit=optionalMoney(r.deposit,"deposit",rowNumber);p.propertyType=blank(r.furnishing)?"Rental":r.furnishing;p.bedrooms=r.bhkType;p.latitude=r.latitude;p.longitude=r.longitude;p.amenities=blank(r.amenities)?new String[0]:Arrays.stream(r.amenities.split("[,;]")).map(String::trim).filter(s->!s.isBlank()).toArray(String[]::new);p.furnishing=r.furnishing;p.maintenanceCharges=optionalMoney(r.maintenanceCharges,"maintenance charges",rowNumber);p.status="available";p.verified=false;p.negotiable=false;p.durationDays=0;p.commissionEarned=BigDecimal.ZERO; return p; }
  private String required(String value,String label,int row){if(blank(value))throw new IllegalArgumentException("Row "+row+": "+label+" is required");return value.trim();} private BigDecimal money(String value,String label,int row){BigDecimal n=optionalMoney(value,label,row);if(n==null||n.signum()<0)throw new IllegalArgumentException("Row "+row+": "+label+" must be a non-negative number");return n;} private BigDecimal optionalMoney(String value,String label,int row){if(blank(value))return null;try{return new BigDecimal(value.replace(",","").replace("₹","" ).trim());}catch(Exception e){throw new IllegalArgumentException("Row "+row+": "+label+" must be numeric");}} private boolean blank(String s){return s==null||s.isBlank();}
  private JwtService.Claims claims(String value) { return value!=null&&value.startsWith("Bearer ")?jwt.verify(value.substring(7)):null; }
  private Response error(Response.Status status,String message){return Response.status(status).entity(Map.of("message",message)).build();}
  private Map<String,Object> dto(PropertyEntity p) { Map<String,Object> m=new LinkedHashMap<>();m.put("id",p.id);m.put("title",p.title);m.put("locality",p.locality);m.put("rent",p.rent);m.put("type",p.propertyType);m.put("bedrooms",p.bedrooms==null?"":p.bedrooms);m.put("amenities",p.amenities==null?List.of():Arrays.asList(p.amenities));m.put("verified",p.verified);m.put("latitude",p.latitude);m.put("longitude",p.longitude);m.put("status",p.status);m.put("availableFrom",p.availableFrom==null?"":p.availableFrom.toString());m.put("negotiable",p.negotiable);m.put("durationDays",p.durationDays);m.put("commissionEarned",p.commissionEarned==null?0:p.commissionEarned);return m; }

  private record ImportedRow(String address,String buildingName,String houseNumber,String ownerName,String ownerMobile,String bhkType,String rent,String deposit,String furnishing,String amenities,String maintenanceCharges,Double latitude,Double longitude) {
    static ImportedRow from(Map<String,String> v){return new ImportedRow(first(v,"address","locality","location"),first(v,"buildingname","building"),first(v,"housenumber","houseno","flatnumber","flatno"),first(v,"ownername","owner"),first(v,"ownermobilenumber","ownermobile","ownerphone","mobilenumber"),first(v,"bhktype","bhk","bedrooms"),first(v,"rent","monthlyrent","monthlyrental"),first(v,"deposit","securitydeposit"),first(v,"furnishing","furnishedstatus"),first(v,"amenities","buildingamenities"),first(v,"maintenancecharges","maintenance","maintenancecharge"),decimal(v,"latitude"),decimal(v,"longitude"));}
    static String first(Map<String,String> v,String... keys){for(String k:keys){String s=v.get(k);if(s!=null&&!s.isBlank())return s.trim();}return "";} static Double decimal(Map<String,String> v,String key){try{String s=v.get(key);return s==null||s.isBlank()?null:Double.valueOf(s.trim());}catch(Exception e){return null;}}
  }
}
