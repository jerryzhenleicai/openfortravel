
package org.jpenguin;

/**
 * Class declaration
 *
 *
 * @author
 * @version %I%, %G%
 */
public class Settings
{
   static public final String    USER_LOGIN_SESSION_ATTR_NAME = "_Jpenguin_User_Login_Session";
   static public final String   HtmlContentType = "text/html;charset=gb2312";
   static public final String   HTML_PAGE_ENCODING = "gb2312";
   static public final String   local_subnet_prefix = "192.168.1";
   static public final String    home_url = "http://www.gaocan.com";
   static public final String   gb_html_header =
     "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">";

   static public final String    hotel_servlet_url = "/travel/hotel.jsp";
   static public final String    review_form_servlet_url = "/reviewform";
   static public final String    add_item_servlet_url = "/additem";

   static public final int       hotel_category_id = 105;
   static public final int       city_category_id = 149;
   static public final int       train_line_category_id = 147;
   static public final int       restaurant_category_id = 131;


   // control how many reviews are shown per page
   static public final int	    review_pages_shown_per_page = 20;


   static public final int user_disk_quota = 10;
   static public final int user_initial_balance_cents = 500;

   static public  String getHomeUrl ()
   {
        return home_url;
   }
   static public  String getHomeUrl2 ()
   {
    return home_url;
   }

}



