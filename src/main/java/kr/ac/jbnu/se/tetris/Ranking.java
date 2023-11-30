package kr.ac.jbnu.se.tetris;

import org.json.JSONArray;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Ranking extends JPanel {
    private transient List<RankingEntry> rankingList;
    private static final Logger logger = Logger.getLogger(Ranking.class.getName());

    public Ranking(Tetris tetris) {
        setLayout(new BorderLayout());

        // 상단 패널
        JLabel titleLabel = new JLabel("랭킹", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 32));
        add(titleLabel, BorderLayout.NORTH);

        // 중앙 패널
        JPanel centerPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        centerPanel.setBackground(Color.WHITE);

        // 백엔드 서버로부터 랭킹 정보를 가져오는 코드
        fetchRankingData(); // 백엔드 서버와 통신하여 데이터 가져오기

        // 랭킹 정보를 표시
        if (rankingList != null) {
            for (RankingEntry entry : rankingList) {
                JLabel idLabel = new JLabel("아이디: " + entry.id());
                JLabel scoreLabel = new JLabel("최고 점수: " + entry.score());
                centerPanel.add(idLabel);
                centerPanel.add(scoreLabel);
            }
        } else {
            JLabel errorLabel = new JLabel("랭킹 정보를 불러올 수 없습니다.", SwingConstants.CENTER);
            centerPanel.add(errorLabel);
        }

        JScrollPane scrollPane = new JScrollPane(centerPanel);
        add(scrollPane, BorderLayout.CENTER);

        // 뒤로 가기 버튼
        JButton backButton = new JButton("뒤로 가기");
        backButton.setBackground(new Color(70, 130, 180));
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        backButton.addActionListener(e ->
            tetris.switchPanel(new MainMenu(tetris))
        );
        add(backButton, BorderLayout.SOUTH);
    }

    private void fetchRankingData() {
        try {
            URL url = new URL("http://localhost:3000/ranking"); // 랭킹 조회 엔드포인트 URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 서버로부터 응답 받기
            InputStream responseStream = connection.getInputStream();
            // 응답 데이터를 문자열로 읽어오기
            String responseData = new String(responseStream.readAllBytes(), StandardCharsets.UTF_8);
            responseStream.close();

            // JSON 데이터 파싱
            JSONArray rankingArray = new JSONArray(responseData);

            // 랭킹 정보를 리스트에 저장
            rankingList = new ArrayList<>();
            for (int i = 0; i < rankingArray.length(); i++) {
                JSONObject rankingObj = rankingArray.getJSONObject(i);
                String id = rankingObj.getString("user_id");
                int score = rankingObj.getInt("max_score");
                RankingEntry entry = new RankingEntry(id, score);
                rankingList.add(entry);
            }

            // 최고 기록을 기준으로 랭킹을 정렬
            rankingList.sort(Comparator.comparingInt(RankingEntry::score).reversed());

        } catch (IOException e) {
            // 서버 통신 오류 처리
            logger.log(Level.SEVERE, "서버 통신 중 에러 발생", e);
            rankingList = null;
        } catch (Exception ex) {
            // JSON 파싱 오류 처리
            logger.log(Level.SEVERE, "JSON 파싱 중 에러 발생", ex);
            rankingList = null;
        }
    }

    private record RankingEntry(String id, int score) {
    }
}
