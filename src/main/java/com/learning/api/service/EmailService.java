package com.learning.api.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.learning.api.config.MailConfig;
import com.learning.api.dto.EmailBookingDTO;
import com.learning.api.dto.EmailBookingTimeDTO;
import com.learning.api.dto.FeedbackEmailDTO;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    private static final Logger log =
            LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MailConfig mailConfig;

    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailConfig.getFrom()); // 從 config 抓
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
    
    public void sendBookingEmail(EmailBookingDTO dto) {

        try {

            String html = buildBookingHtml(dto);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(dto.getTutorEmail());
            helper.setSubject("【課程預約通知】" + dto.getStudentName());
            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {

        	log.error("Email 發送失敗 tutorEmail={}，原因={}", 
        	          dto.getTutorEmail(), 
        	          e.getMessage());

        }
    }
    
    public void sendFeedbackEmail(FeedbackEmailDTO dto) {

        try {

            String html = buildFeedbackHtml(dto);

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(dto.getStudentEmail());
            helper.setSubject("【課程回饋】" + dto.getCourseName());
            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {

            log.error("Feedback Email 發送失敗 email={}", dto.getStudentEmail(), e);

        }

    }
    
    private String formatDate(String isoDate) {
        LocalDate date = LocalDate.parse(isoDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        return date.format(formatter);
    }

    private String stars(int score) {

    	  return  "<span style='font-size:16px; color:gold;'>★</span>".repeat(score)
    		         + "<span style='font-size:16px; color:#ccc;'>☆</span>".repeat(5 - score);


    }

	private String buildBookingHtml(EmailBookingDTO dto) {
		// TODO Auto-generated method stub

		 final String TEMPLATE = """
				<div style="background:#f5f7fb;padding:30px;font-family:Arial,Helvetica,sans-serif;">

				<table align="center" width="600" style="background:#ffffff;border-radius:8px;padding:30px;border-collapse:collapse;">

				<tr>
				<td>

				<h2 style="color:#333;margin-bottom:20px;"> 新課程預約通知</h2>

				<p style="font-size:15px;color:#555;">
				<strong>{tutorName}</strong> 您好，
				</p>

				<p style="font-size:15px;color:#555;">
				<strong>{studentName}</strong> 訂購了課程
				<strong>{courseName}</strong> 共
				<strong style="color:#2a7be4;">{amount}</strong> 堂課
				</p>

				<br>

				<table width="100%" style="border-collapse:collapse;font-size:14px;">

				<tr style="background:#2a7be4;color:#ffffff;">
				<th style="padding:10px;border:1px solid #e5e5e5;">編號</th>
				<th style="padding:10px;border:1px solid #e5e5e5;">預約時間</th>
				</tr>

				{rows}

				</table>

				</td>
				</tr>

				</table>

				</div>
				""";

		    String rows = "";

		    int i = 1;

		    for (EmailBookingTimeDTO t : dto.getTimes()) {

		        rows += """
		        <tr>
		        <td style="padding:10px;border:1px solid #e5e5e5;text-align:center;">%d</td>
		        <td style="padding:10px;border:1px solid #e5e5e5;">%s %02d:00 ~ %02d:00</td>
		        </tr>
		        """.formatted(
		                i++,
		                formatDate(t.getDate().toString()),
		                t.getHour(),
		                t.getHour() + 1
		        );
		    }

		    return TEMPLATE
		            .replace("{tutorName}", dto.getTutorName())
		            .replace("{studentName}", dto.getStudentName())
		            .replace("{courseName}", dto.getCourseName())
		            .replace("{amount}", String.valueOf(dto.getTimes().size()))
		            .replace("{rows}", rows);
		}
	
	private String buildFeedbackHtml(FeedbackEmailDTO dto) {
		final String template = """
	        <div style="background:#f5f7fb;padding:30px;font-family:Arial,Helvetica,sans-serif;">
	          <table align="center" width="600" style="background:#ffffff;border-radius:8px;padding:30px;border-collapse:collapse;">
	            <tr>
	              <td>
	                <h2 style="color:#333;margin-bottom:20px;">📚 課程回饋通知</h2>
	                <table width="100%" style="border-collapse:collapse;font-size:14px;margin-bottom:15px;background:#f8f9fb;">
	                  <tr>
	                    <td style="padding:10px;">學生：<strong>{studentName}</strong></td>
	                    <td style="padding:10px;">老師：<strong>{tutorName}</strong></td>
	                    <td style="padding:10px;">課程：<strong>{courseName}</strong></td>
	                  </tr>
	                </table>
	                <p style="font-size:14px;color:#555;">
	                  上課時間：<strong>{date} {hour}:00 ~ {endHour}:00</strong>
	                </p>
	                <br>
	                <table width="100%" style="border-collapse:collapse;font-size:14px;">
	                  <tr style="background:#2a7be4;color:#ffffff;">
	                    <th style="padding:10px;border:1px solid #e5e5e5;">評分項目</th>
	                    <th style="padding:10px;border:1px solid #e5e5e5;">分數</th>
	                  </tr>
	                  <tr>
	                    <td style="padding:10px;border:1px solid #e5e5e5;">參與專注度</td>
	                    <td style="padding:10px;border:1px solid #e5e5e5;">{focusStars}</td>
	                  </tr>
	                  <tr style="background:#fafafa;">
	                    <td style="padding:10px;border:1px solid #e5e5e5;">理解力</td>
	                    <td style="padding:10px;border:1px solid #e5e5e5;">{compStars}</td>
	                  </tr>
	                  <tr>
	                    <td style="padding:10px;border:1px solid #e5e5e5;">口語自信</td>
	                    <td style="padding:10px;border:1px solid #e5e5e5;">{confStars}</td>
	                  </tr>
	                </table>
	                <br>
	                <p style="font-size:14px;color:#555;"><strong>老師評語：</strong></p>
	                <p style="background:#f8f9fb;padding:12px;border-radius:6px;color:#555;">{comment}</p>
	                <hr style="border:none;border-top:1px solid #eee;margin:25px 0;">
	                <p style="font-size:12px;color:#aaa;text-align:center;">此信件為系統自動發送</p>
	              </td>
	            </tr>
	          </table>
	        </div>
	        """;

	    int endHour = dto.getHour() + 1;
	
	    String html = template
	        .replace("{studentName}", dto.getStudentName())
	        .replace("{tutorName}", dto.getTutorName())
	        .replace("{courseName}", dto.getCourseName())
	        .replace("{date}", formatDate(dto.getDate().toString()))
	        .replace("{hour}", String.valueOf(dto.getHour()))
	        .replace("{endHour}", String.valueOf(endHour))
	        .replace("{focusStars}", stars(dto.getFocusScore()))
	        .replace("{compStars}", stars(dto.getComprehensionScore()))
	        .replace("{confStars}", stars(dto.getConfidenceScore()))
	        .replace("{comment}", dto.getComment());
	
	    return html;
	}





}

