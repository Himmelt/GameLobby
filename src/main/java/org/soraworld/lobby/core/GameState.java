package org.soraworld.lobby.core;

/**
 * 游戏大厅状态枚举.
 */
public enum GameState {

    /**
     * 大厅开启.
     */
    OPEN(false, true, false, true),
    /**
     * 游戏开始.
     */
    START(false, false, true, false),
    /**
     * 游戏结束.
     */
    FINISH(true, false, false, true),
    /**
     * 大厅关闭.
     */
    CLOSE(true, false, false, false);

    private final boolean canOpen, canStart, canFinish, canClose;

    GameState(boolean canOpen, boolean canStart, boolean canFinish, boolean canClose) {
        this.canOpen = canOpen;
        this.canStart = canStart;
        this.canFinish = canFinish;
        this.canClose = canClose;
    }

    /**
     * 能否开启大厅.
     *
     * @return 能否开启大厅
     */
    public boolean canOpen() {
        return canOpen;
    }

    /**
     * 能否开始游戏.
     *
     * @return 能否开始游戏
     */
    public boolean canStart() {
        return canStart;
    }

    /**
     * 能否结束游戏.
     *
     * @return 能否结束游戏
     */
    public boolean canFinish() {
        return canFinish;
    }

    /**
     * 能否关闭大厅.
     *
     * @return 能否关闭大厅
     */
    public boolean canClose() {
        return canClose;
    }
}
