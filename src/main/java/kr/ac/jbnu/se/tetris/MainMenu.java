package kr.ac.jbnu.se.tetris;

import org.json.JSONException;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.logging.Level;

public class MainMenu extends JPanel {
    private static final String MAIN_FONT_NAME = "맑은 고딕";
    private final transient Logger logger = Logger.getLogger(getClass().getName());
    private final Tetris tetris;
    private final JPanel topPanel = new JPanel(new BorderLayout());
    private final JPanel centerPanel = new JPanel(new GridLayout(4, 1, 10, 10));
    private final JPanel bottomPanel = new JPanel(new FlowLayout());
    private final JLabel title = new JLabel("테트리스", SwingConstants.CENTER);
    private final JButton normalModeButton = new JButton("기본 모드");
    private final JButton sprintButton = new JButton("스프린트 모드");
    private final JButton timeAttackButton = new JButton("타임어택 모드");
    private final JButton ghostModeButton = new JButton("고스트 모드");
    private final JButton achievementButton = new JButton("업적");
    private final JButton rankingButton = new JButton("랭킹");
    private final JButton settingButton = new JButton("설정");
    private final JPopupMenu difficultyPopupMenu = new JPopupMenu();
    private final String userId;
    private final int userMaxScore;
    private final int userLevel;
    private String currentGameMode;
    private static final String SERVER_URL = "http://localhost:3000";
    
    public MainMenu(Tetris tetris) {
        this.tetris = tetris;
        this.userId = tetris.getUserId();
        this.userMaxScore = getMaxScoreFromServer(userId);
        this.userLevel = tetris.getUserLevel();
        this.currentGameMode = tetris.getCurrentGameMode(); // 초기화

        initUI();
        sendUserMaxScoreToServer();
    }

    // UI 초기화
    private void initUI(){
        setLayout(new FlowLayout());

        addTopPanel();
        addCenterPanel();
        addBottomPanel();
    }

    // 상단 패널에 제목과 사용자 정보 추가
    private void addTopPanel(){
        JLabel profileLabel;
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 10, 50));

        title.setFont(new Font(MAIN_FONT_NAME, Font.BOLD, 32));
        topPanel.add(title, BorderLayout.NORTH);
        
        profileLabel = new JLabel("ID : " + userId + " | Level : " + userLevel + " | 최고 기록 : " + userMaxScore, SwingConstants.CENTER);
        profileLabel.setFont(new Font(MAIN_FONT_NAME, Font.PLAIN, 16));
        topPanel.add(profileLabel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
    }

    private void addCenterPanel(){
        // 기본 모드 버튼
        normalModeButton.addActionListener(e -> showDifficultyPopupMenu());
        centerPanel.add(setStyledButton(normalModeButton, 200, 45));

        // 스프린트 모드 버튼
        sprintButton.addActionListener(e -> tetris.switchPanel(new SprintMode(tetris)));
        centerPanel.add(setStyledButton(sprintButton, 200, 45));
        

        // 타임어택 모드 버튼
        timeAttackButton.addActionListener(e -> tetris.switchPanel(new TimeAttackMode(tetris)));
        centerPanel.add(setStyledButton(timeAttackButton, 200, 45));
        
        // 고스트 모드 버튼
        ghostModeButton.addActionListener(e -> tetris.switchPanel(new GhostMode(tetris)));
        centerPanel.add(setStyledButton(ghostModeButton, 200, 45));

        // 중앙 패널 설정
        centerPanel.setBorder(BorderFactory.createTitledBorder("게임 모드"));
        centerPanel.setPreferredSize(new Dimension(250, 200));
        add(centerPanel, BorderLayout.CENTER);
    }

    // 기본모드 난이도 선택 팝업 메뉴
    private void showDifficultyPopupMenu(){        
        String[] difficulty = {"Easy", "Normal", "Hard", "Very Hard", "God"};
        for(String diff : difficulty){
            JMenuItem menuItem = new JMenuItem(diff);
            menuItem.addActionListener(e -> tetris.switchPanel(new Board(tetris, diff)));
            currentGameMode = diff; // 선택된 모드를 설정
            tetris.setCurrentGameMode(diff); // Tetris 인스턴스에도 설정
            difficultyPopupMenu.add(menuItem);
        }
        difficultyPopupMenu.show(normalModeButton, normalModeButton.getWidth() / 2, normalModeButton.getHeight());
    }

    private void addBottomPanel(){
        // 업적 관리 버튼
        achievementButton.addActionListener(e ->tetris.switchPanel(new AchievementMenu(tetris)));
        bottomPanel.add(setStyledButton(achievementButton, 75, 40));
        
        // 랭킹 버튼
        rankingButton.addActionListener(e -> tetris.switchPanel(new ModeSelection(tetris)));
        bottomPanel.add(setStyledButton(rankingButton, 75, 40));

        // 설정 버튼
        settingButton.addActionListener(e -> tetris.switchPanel(new SettingMenu(tetris)));
        bottomPanel.add(setStyledButton(settingButton, 75, 40));

        // 하단 패널에 버튼 추가
        bottomPanel.setPreferredSize(new Dimension(350, 60));
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // 버튼 스타일 설정
    private JButton setStyledButton(JButton button, int width, int height){
        button.setPreferredSize(new Dimension(width, height));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFont(new Font(MAIN_FONT_NAME, Font.BOLD, 13));
        button.setFocusPainted(false);
        return button;
    }

    // 사용자 ID와 최고 점수를 올바르게 전달 백엔드
    private void sendUserMaxScoreToServer() {
        String id = tetris.getUserId();
        int maxScore = tetris.getUserMaxScore();

        boolean scoreSent = sendScoreToServer(id, maxScore, currentGameMode);

        logScoreSendingResult(scoreSent);
    }

    private void logScoreSendingResult(boolean scoreSent) {
        if (scoreSent) {
            logger.info("서버로 최고 점수 전송 성공.");
        } else {
            logger.warning("서버로 최고 점수를 전송하는 데 실패했습니다.");
        }
    }


    // Existing method to send the score to the server
    private boolean sendScoreToServer(String userId, int maxScore, String currentGameMode) {
        try {
        	URL url = new URL(SERVER_URL + "/score");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // JSON format for the request body with game mode
            // 클라이언트에서 score 엔드포인트 호출 시 mode 정보도 함께 보내기
            String jsonInputString = "{ \"user_id\": \"" + userId + "\", \"score\": " + maxScore + ", \"mode\": \"" + currentGameMode + "\" }";

            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(input, 0, input.length);
            }

            // Response code verification
            int responseCode = connection.getResponseCode();
            if (responseCode == 201) {
                return true; // Score sent successfully
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false; // Score sending failed
    }

    // 서버로부터 최고 점수를 가져오는 메소드
    private int getMaxScoreFromServer(String userId) {
        try {
            JSONObject jsonResponse = getJsonObject(userId);

            // 최고 점수 추출
            return jsonResponse.getInt("max_score");

        } catch (IOException e) {
            // 서버 통신 오류 처리
            logger.log(Level.SEVERE, "서버 통신 중 에러 발생", e);
        } catch (Exception ex) {
            // JSON 파싱 오류 처리
            logger.log(Level.SEVERE, "JSON 파싱 중 에러 발생", ex);
        }

        return 0; // 요청 실패 시 기본값 반환
    }

    private static JSONObject getJsonObject(String userId) throws IOException, JSONException {
        URL url = new URL("http://localhost:3000/showPanelMaxScore?user_id=" + userId); // 서버의 엔드포인트 URL로 업데이트
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // 서버로부터 응답 받기
        InputStream responseStream = connection.getInputStream();
        // 응답 데이터를 문자열로 읽어오기
        String responseData = new String(responseStream.readAllBytes(), StandardCharsets.UTF_8);
        responseStream.close();

        // JSON 데이터 파싱
        return new JSONObject(responseData);
    }
}
