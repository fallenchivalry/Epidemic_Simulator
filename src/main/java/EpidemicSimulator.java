import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EpidemicSimulator {

    // sir model

    static double INFECTION_RADIUS      = 15.0;
    static double INFECTION_PROBABILITY = 0.30;   // per tick when in radius
    static int    RECOVERY_TICKS        = 300;     // ticks until recovery
    static double DEATH_RATE            = 0.02;    // fraction of infected who die
    static int    POPULATION_SIZE       = 200;
    static double MOVEMENT_SPEED        = 1.5;

    // simulation area

    static final int SIM_W = 800;
    static final int SIM_H = 560;
    static final int BLOB_R = 6;

    // enum state

    enum State { SUSCEPTIBLE, INFECTED, RECOVERED, DEAD }

    // person
    static class Person {
        double x, y, vx, vy;
        State state;
        int infectionTimer;
        boolean willDie;

        static final Random random = new Random();

        Person(double x, double y) {
            this.x = x;
            this.y = y;
            double angle = random.nextDouble() * 2 * Math.PI;
            this.vx = Math.cos(angle) * MOVEMENT_SPEED;
            this.vy = Math.sin(angle) * MOVEMENT_SPEED;
            this.state = State.SUSCEPTIBLE;
            this.infectionTimer = 0;
            this.willDie = false;
        }

        void infect() {
            if (state == State.SUSCEPTIBLE) {
                state = State.INFECTED;
                infectionTimer = RECOVERY_TICKS;
                willDie = random.nextDouble() < DEATH_RATE;
            }
        }

        void update() {
            if (state == State.DEAD) return;

            // Move
            x += vx;
            y += vy;

            // wall limit
            if (x - BLOB_R < 0)      { x = BLOB_R;        vx = Math.abs(vx); }
            if (x + BLOB_R > SIM_W)  { x = SIM_W - BLOB_R; vx = -Math.abs(vx); }
            if (y - BLOB_R < 0)      { y = BLOB_R;        vy = Math.abs(vy); }
            if (y + BLOB_R > SIM_H)  { y = SIM_H - BLOB_R; vy = -Math.abs(vy); }

            // infeciton
            if (state == State.INFECTED) {
                infectionTimer--;
                if (infectionTimer <= 0) {
                    state = willDie ? State.DEAD : State.RECOVERED;
                    if (state == State.DEAD) { vx = 0; vy = 0; }
                }
            }
        }

        Color color() {
            return switch (state) {
                case SUSCEPTIBLE -> new Color(35, 93, 152);   // blue
                case INFECTED    -> new Color(141, 0, 0);    // red
                case RECOVERED   -> new Color(60, 180, 90);    // green
                case DEAD        -> new Color(120, 120, 120);  // gray
            };
        }

        double distanceTo(Person other) {
            double dx = this.x - other.x;
            double dy = this.y - other.y;
            return dx * dx + dy * dy;
        }
    }

    // panelll

    static class panel extends JPanel {
        List<Person> people = new ArrayList<>();
        Random rng = new Random();
        int tick = 0;
        boolean running = false;


        List<int[]> history = new ArrayList<>(); // [S, I, R, D] per tick

        panel() {
            setPreferredSize(new Dimension(SIM_W, SIM_H + 120)); // +120 for graph
            setBackground(new Color(18, 18, 24));
            reset(1);
        }

        void reset(int initialInfected) {
            people.clear();
            history.clear();
            tick = 0;
            running = false;

            for (int i = 0; i < POPULATION_SIZE; i++) {
                double x = BLOB_R + rng.nextDouble() * (SIM_W - 2 * BLOB_R);
                double y = BLOB_R + rng.nextDouble() * (SIM_H - 2 * BLOB_R);
                people.add(new Person(x, y));
            }

            for (int i = 0; i < initialInfected; i++) {
                people.get(i).infect();
            }
            repaint();
        }

        void step() {
            if (!running) return;
            tick++;

            for (Person p : people) p.update();

            // Spread infection
            for (Person a : people) {
                if (a.state != State.INFECTED) continue;
                for (Person b : people) {
                    if (b.state != State.SUSCEPTIBLE) continue;
                    if (a.distanceTo(b) < INFECTION_RADIUS) {
                        if (rng.nextDouble() < INFECTION_PROBABILITY) {
                            b.infect();
                        }
                    }
                }
            }

            // Record history
            int s = 0, inf = 0, r = 0, d = 0;
            for (Person p : people) {
                switch (p.state) {
                    case SUSCEPTIBLE -> s++;
                    case INFECTED    -> inf++;
                    case RECOVERED   -> r++;
                    case DEAD        -> d++;
                }
            }
            history.add(new int[]{s, inf, r, d});

            repaint();
        }

        int[] counts() {
            int s = 0, inf = 0, r = 0, d = 0;
            for (Person p : people) {
                switch (p.state) {
                    case SUSCEPTIBLE -> s++;
                    case INFECTED    -> inf++;
                    case RECOVERED   -> r++;
                    case DEAD        -> d++;
                }
            }
            return new int[]{s, inf, r, d};
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


            g.setColor(new Color(60, 60, 80));
            g.setStroke(new BasicStroke(2f));
            g.drawRect(0, 0, SIM_W - 1, SIM_H - 1);


            g.setStroke(new BasicStroke(1f));
            for (Person p : people) {
                if (p.state == State.INFECTED) {
                    int r = (int) INFECTION_RADIUS;
                    g.setColor(new Color(220, 50, 50, 30));
                    g.fillOval((int)(p.x - r), (int)(p.y - r), r * 2, r * 2);
                    g.setColor(new Color(220, 50, 50, 60));
                    g.drawOval((int)(p.x - r), (int)(p.y - r), r * 2, r * 2);
                }
            }

            for (Person p : people) {
                Color c = p.color();
                // Glow effect
                if (p.state == State.INFECTED) {
                    g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 60));
                    g.fillOval((int)(p.x - BLOB_R - 3), (int)(p.y - BLOB_R - 3),
                            (BLOB_R + 3) * 2, (BLOB_R + 3) * 2);
                }
                g.setColor(c);
                g.fillOval((int)(p.x - BLOB_R), (int)(p.y - BLOB_R), BLOB_R * 2, BLOB_R * 2);

            }

            drawGraph(g);

            g.setColor(new Color(160, 160, 180));
            g.setFont(new Font("Monospaced", Font.PLAIN, 11));
            g.drawString("tick " + tick, 8, SIM_H - 6);
        }

        void drawGraph(Graphics2D g) {
            int gx = 0, gy = SIM_H + 1;
            int gw = SIM_W, gh = 118;

            g.setColor(new Color(24, 24, 32));
            g.fillRect(gx, gy, gw, gh);
            g.setColor(new Color(50, 50, 70));
            g.drawRect(gx, gy, gw - 1, gh - 1);

            if (history.isEmpty()) return;

            int n = history.size();
            double xScale = (double) gw / Math.max(n, 1);
            double yScale = (double) (gh - 10) / POPULATION_SIZE;

            Color[] colors = {
                    new Color(70, 130, 200),
                    new Color(220, 50, 50),
                    new Color(60, 180, 90),
                    new Color(120, 120, 120)
            };

            for (int ci = 0; ci < 4; ci++) {
                g.setColor(colors[ci]);
                g.setStroke(new BasicStroke(1.5f));
                int[] prev = history.get(0);
                for (int i = 1; i < n; i++) {
                    int[] curr = history.get(i);
                    int x1 = gx + (int)((i - 1) * xScale);
                    int y1 = gy + gh - 5 - (int)(prev[ci] * yScale);
                    int x2 = gx + (int)(i * xScale);
                    int y2 = gy + gh - 5 - (int)(curr[ci] * yScale);
                    g.drawLine(x1, y1, x2, y2);
                    prev = curr;
                }
            }

            String[] labels = {"Susceptible", "Infected", "Recovered", "Dead"};
            g.setFont(new Font("Monospaced", Font.BOLD, 11));
            int lx = 8;
            for (int ci = 0; ci < 4; ci++) {
                g.setColor(colors[ci]);
                g.fillOval(lx, gy + 6, 8, 8);
                g.drawString(labels[ci], lx + 12, gy + 15);
                lx += 100;
            }
        }
    }

    // main

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("epidemic simulator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            panel sim = new panel();

            JPanel controls = new JPanel();
            controls.setBackground(new Color(28, 28, 38));
            controls.setLayout(new GridBagLayout());
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(4, 8, 4, 8);
            gc.fill = GridBagConstraints.HORIZONTAL;

            int row = 0;

            // Infection Radius
            row = addSlider(controls, gc, row, "Infection Radius",
                    1, 60, (int) INFECTION_RADIUS, val -> {
                        INFECTION_RADIUS = val;
                    });

            // Infection Probability
            row = addSlider(controls, gc, row, "Infection % (×10)",
                    1, 100, (int)(INFECTION_PROBABILITY * 100), val -> {
                        INFECTION_PROBABILITY = val / 100.0;
                    });

            int[] startInfected = {1};

            row = addSlider(controls, gc, row, "Starting Infected",
                    1, 20, 1, val -> {
                        startInfected[0] = val;
                    });
            // Recovery Ticks
            row = addSlider(controls, gc, row, "Recovery Ticks (×10)",
                    5, 100, RECOVERY_TICKS / 10, val -> {
                        RECOVERY_TICKS = val * 10;
                    });

            // Death Rate
            row = addSlider(controls, gc, row, "Death Rate % (×10)",
                    0, 100, (int)(DEATH_RATE * 100), val -> {
                        DEATH_RATE = val / 100.0;
                    });

            int[] simSpeed = {1};

            row = addSlider(controls, gc, row, "Sim Speed (×ticks)",
                    1, 10, 1, val -> {
                        simSpeed[0] = val;
                    });

            // Population
            row = addSlider(controls, gc, row, "Population",
            10, 500, POPULATION_SIZE, val -> {
            POPULATION_SIZE = val;
            sim.reset(startInfected[0]);
        });

            // buttonsss
            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
            btnRow.setBackground(new Color(28, 28, 38));

            JButton startBtn = styledButton("Start");
            JButton pauseBtn = styledButton("Pause");
            JButton resetBtn = styledButton("Reset");

            startBtn.addActionListener(e -> sim.running = true);
            pauseBtn.addActionListener(e -> sim.running = false);
            resetBtn.addActionListener(e -> { sim.reset(startInfected[0]); sim.repaint(); });

            btnRow.add(startBtn);
            btnRow.add(pauseBtn);
            btnRow.add(resetBtn);

            gc.gridx = 0; gc.gridy = row; gc.gridwidth = 3;
            controls.add(btnRow, gc);

            JLabel stats = new JLabel("S:0  I:0  R:0  D:0");
            stats.setForeground(new Color(200, 200, 220));
            stats.setFont(new Font("Monospaced", Font.PLAIN, 12));
            stats.setHorizontalAlignment(SwingConstants.CENTER);
            gc.gridy = row + 1;
            controls.add(stats, gc);

            Timer timer = new Timer(16, e -> {
                for (int i = 0; i < simSpeed[0]; i++) {
                    sim.step();
                }
                int[] c = sim.counts();
                stats.setText(String.format(
                        "  S: %d   I: %d   R: %d   D: %d",
                        c[0], c[1], c[2], c[3]
                ));
            });
            timer.start();

            frame.setLayout(new BorderLayout());
            frame.add(sim, BorderLayout.CENTER);
            frame.add(controls, BorderLayout.EAST);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    //slider help

    interface IntConsumer { void accept(int val); }

    static int addSlider(JPanel panel, GridBagConstraints gc,
                         int row, String label, int min, int max, int init,
                         IntConsumer onChange) {
        gc.gridwidth = 1;

        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(180, 180, 210));
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        panel.add(lbl, gc);

        JSlider slider = new JSlider(min, max, init);
        slider.setBackground(new Color(28, 28, 38));
        slider.setForeground(new Color(100, 160, 255));
        slider.setPreferredSize(new Dimension(160, 24));
        gc.gridx = 1; gc.weightx = 1;
        panel.add(slider, gc);

        JLabel valLbl = new JLabel(String.valueOf(init));
        valLbl.setForeground(new Color(220, 180, 100));
        valLbl.setFont(new Font("Monospaced", Font.BOLD, 11));
        valLbl.setPreferredSize(new Dimension(36, 20));
        gc.gridx = 2; gc.weightx = 0;
        panel.add(valLbl, gc);

        slider.addChangeListener(e -> {
            int v = slider.getValue();
            valLbl.setText(String.valueOf(v));
            onChange.accept(v);
        });

        return row + 1;
    }

    //button

    static JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(50, 60, 90));
        btn.setForeground(new Color(210, 220, 255));
        btn.setFont(new Font("Monospaced", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 100, 160), 1),
                BorderFactory.createEmptyBorder(5, 14, 5, 14)
        ));
        return btn;
    }
}