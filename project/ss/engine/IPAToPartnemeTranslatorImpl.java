//Source file: C:\\project\\ss\\engine\\IPAToPartnemeTranslatorImpl.java

package project.ss.engine;

import project.ss.misc.*;
import project.ss.exception.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.nio.*;

/**
 * This class implememts the IPAToPartnemeTranslation interface
 * And use various methods to provide the translation.
 * It provides a way to manupilate the the idle parteneme tranlation
 * through finalProcessing() method.see  the method description for more detail.
 */
public class IPAToPartnemeTranslatorImpl implements IPAToPartnemeTranslator 
{
   
   /**
    * This array holds the pairs of partneme where one should be replaced by the other
    * in the finalProcessing () method.
    */
   static String tokens[];
   
   /**
    * Constructs an object of this type with loading the partneme replacment
    * rules from the file specified.
    * 
    * @param lookUpFile
    * @param encoding
    * @roseuid 3B06A65E02B4
    */
   public IPAToPartnemeTranslatorImpl(File lookUpFile, String encoding) 
   {
     CharBuffer charBuffer= FileLoad.getCharBufferRO(lookUpFile,encoding);
//     Pattern pp = Pattern.compile("\r\n");  // change \r\n to other for multi OS compatibility
     Pattern pp = Pattern.compile("[\r\n]+");  // change \r\n to other for multi OS compatibility
     tokens = pp.split(charBuffer.toString());
     for (int i=0;i<tokens.length;i++)
      {
          System.out.println("Token  :" );
          System.out.println("Token "+ i + " :" + tokens[i] + "_______");
      }    
   }
   
   /**
    * This method is an implementaion of the similar method 
    * described in the interface
    * @param validIpaString
    * @return java.lang.String
    * @throws project.ss.exception.ImproperIPASequence -  aspects that ipaString is 
    * valid doesnt do validation
    * @roseuid 3B06A65E02FA
    */
   public String getPartnemeString(String validIpaString) throws ImproperIPASequence 
   {
         if (false )//confirmTotheSyntax())   // it checks weather the ipaString is in
                                     // proper syntax so tha tokenizer will
                                     // not be misguided
                                     // {[ipachars] [ipachars] .. [ipachars]}{.....}{....} ...  
         {
            System.out.println("ImproperIPASequence  :" + validIpaString );
            throw new  ImproperIPASequence (validIpaString );
         }

      // segment the ipa into sentance boundries
      // for each sentance boundry segment the ipa into utterance boundries
      // convert each utterance to partneme
      String partnemeString = ipaToPartneme (validIpaString);
      System.out.println ( "IPA String  :\n"  +validIpaString );
      String finalPartnemeString = finalProcessing(partnemeString); // replacing for exceptiona
      System.out.println ( "Final Partneme String   :\n" );
      System.out.println (  finalPartnemeString );
      return finalPartnemeString ;    
   }
   
   /**
    * @param validIpaString
    * @return java.lang.String
    * @roseuid 3B06A65E035E
    */
   protected static String ipaToPartneme(String validIpaString) 
   {
      String partnemeString="";
      String sentance = "";
      StringTokenizer stForSentance = new StringTokenizer (validIpaString,IPAChars.DIA_SENT_OPEN+IPAChars.DIA_SENT_CLOSE);
      for ( ; stForSentance.hasMoreTokens();) // loop for sentance breaking
      {
         sentance = stForSentance.nextToken();
         partnemeString+=sentanceToPartneme(sentance)+";0;0;0;"; // ";0;0;0;" for silence in between sentances/juncture
      }
      return partnemeString;    
   }
   
   /**
    * @param ipaSentance
    * @return java.lang.String
    * @roseuid 3B06A65E039A
    */
   protected static String sentanceToPartneme(String ipaSentance) 
   {
     String partnemeString="";
     String utterance = "";
     StringTokenizer stForUtterance = new StringTokenizer (ipaSentance,IPAChars.DIA_UTT_OPEN+IPAChars.DIA_UTT_CLOSE+" \t\n");
     for ( ; stForUtterance.hasMoreTokens();) // loop for utterance breaking
          {
             utterance = stForUtterance.nextToken();
             partnemeString+=utteranceToPartneme(utterance)+";0;0;"; // ";0;0;" for silence in between utterances
          }
     return partnemeString ;    
   }
   
   /**
    * @param ipaUtterance
    * @return java.lang.String
    * @roseuid 3B06A65E03CC
    */
   protected static String utteranceToPartneme(String ipaUtterance) 
   {
     /* utteranceString cant have
        +,[,],{,},and whitespace char  put validation here
     */
     System.out.println ( "IPA Utterance :\n" + ipaUtterance);
     String partnemeString="";
     int ConsoCount=0;
     String prevPhoneme="0";
     String phonemeMarkedIpaUtterance = phonemeMarker(ipaUtterance,"#");
     System.out.println ( " Marked (#) IPA Utterance :\n" + phonemeMarkedIpaUtterance);
     StringTokenizer stForPhoneme = new StringTokenizer (phonemeMarkedIpaUtterance,"#");
     for ( ;stForPhoneme.hasMoreTokens();)
     {
         String ipaPhoneme = stForPhoneme.nextToken();
         if ( IPAChars.isVowel(ipaPhoneme))
         {
           System.out.println("Vowel found : "+ ipaPhoneme);
           partnemeString+= prevPhoneme +"_"+ ipaPhoneme+";"+ ipaPhoneme;
           prevPhoneme=ipaPhoneme;
         }
         else if ( IPAChars.isConso(ipaPhoneme))
         {
           System.out.println("Conso found : "+ ipaPhoneme);
           partnemeString+= prevPhoneme +"_"+ ipaPhoneme + ";"+ ipaPhoneme;
           prevPhoneme=ipaPhoneme;
         }
         else if ( IPAChars.isUnSyllVowel(ipaPhoneme))
         {
           System.out.println("UnSyllVowel found : "+ ipaPhoneme);
           partnemeString+= prevPhoneme +"_"+ ipaPhoneme + ";"+ ipaPhoneme;
           prevPhoneme=ipaPhoneme;
         }
         else
         {}//GSS Exce
         partnemeString+=";";
     }

     partnemeString+= prevPhoneme+"_0";
     System.out.println ( "  Utterance's  Partneme String (IDLE) :\n" + partnemeString);
     return partnemeString ;    
   }
   
   /**
    * @param ipaUtterance
    * @param delmSign
    * @return java.lang.String
    * @roseuid 3B06A65F0020
    */
   protected static String phonemeMarker(String ipaUtterance, String delmSign) 
   {
       // put delm inbetween every phoneme
       // put if C,C
       // put if C,V or V,C
       // put if V,V
      String phonemeMarkedIpaUtterance="";
      for (int i=0; i < ipaUtterance.length();i++)
        {
          String ipaChar = ipaUtterance.substring(i,i+1);
          if ( ipaChar.equals(IPAChars.DIA_ASPI)|| ipaChar.equals(IPAChars.DIA_ALONG)||
               ipaChar.equals(IPAChars.DIA_NASS)|| ipaChar.equals(IPAChars.DIA_UNSYL)
             )
           {
             phonemeMarkedIpaUtterance+=ipaChar;
           }
          else
           {
             phonemeMarkedIpaUtterance+=delmSign+ipaChar;
           }
        }
      return phonemeMarkedIpaUtterance+delmSign;    
   }
   
   /**
    * To dictate the partneme sequence created by  the ipaToPartneme()
    * method for context specific modification.
    * This method uses the text file to modify the partneme seqence.
    * We can specify the new partneme string in stead of the other.
    * This function is very useful in sorting out the consonent conjucts 
    * in the partneme sequence.
    * 
    * @param partnemeString
    * @return java.lang.String
    * @roseuid 3B06A65F0070
    */
   protected static String finalProcessing(String partnemeString) 
   {

  // this method will serve as partneme to label(Filename) converter as well as
  // in handling the exceptional rules of concatenation
     StringBuffer tobeMatched = new  StringBuffer (partnemeString);
     System.out.println(tobeMatched.toString());
     System.out.println(tobeMatched.length());
     String searchString=null;
     String replacingString=null;
     String exPartnemePair[]=null;  // Exceptional  partneme seq
     Pattern pp = Pattern.compile("=");
     for (int i=1;i<tokens.length;i++)
      {
        if ( tokens[i].indexOf("=") != -1)
         {
          exPartnemePair = pp.split(tokens[i]);
          searchString= exPartnemePair[0];
          replacingString= exPartnemePair[1];
          Pattern pattern = Pattern.compile(searchString);
          Matcher matcher = pattern.matcher(tobeMatched);
          tobeMatched = new StringBuffer(matcher.replaceAll(replacingString));
          System.out.println(tobeMatched.toString());
          System.out.println(tobeMatched.length());
         }
      }
     return   tobeMatched.toString();    
   }
}
