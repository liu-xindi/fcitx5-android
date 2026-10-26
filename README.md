# fcitx5-android

[Fcitx5](https://github.com/fcitx/fcitx5) input method framework and engines ported to Android.

## 关于 fork

基于 [fcitx5-android](https://github.com/fcitx5-android/fcitx5-android) 0.1.3，修改了键盘布局和包名。

- 第一行 q–p：按设置的划动方向输出 `[ ] { } < > ; : & |`，反方向输出数字 1–0。
- z x：划动分别输出 `_` `'`。
- 句号键：上下划动输出 `$`。
- Shift 键：上下划动输出 4 个空格。
- 反方向划动 z f g 输出 `` ` `` `%` `^`。
- 编辑：反方向划动 h j k l 移动光标，b n 移到行首、行尾，a s x c v 分别全选、切换选择模式、剪切、复制、粘贴（键上以 `•` 标出）。选择模式下移动会扩展选区。输入拼音时 h l 在拼音内移动，其他编辑划动不生效。
- 空格键：开启滑动移动光标后，上下拖动也能移动光标。一次拖动只沿先移动的方向，选择模式和 Ctrl 同样生效。
- ?123 键：上下划动开关 Ctrl，开启时上述移动带 Ctrl，如 h l 按词跳。输入其他内容或切换键盘后关闭。
- 长按菜单：加入对应的划动符号。
- 包名：`com.liuxindi.fcitx5`，可与官方版共存，官方版的备份不能直接导入。
- 插件：官方插件按官方包名查找主程序，无法使用（其中剪贴板过滤插件还要求签名一致）。使用官方插件需要修改包名自行编译。

上游项目的介绍、支持的语言和编译方法见 [fcitx5-android](https://github.com/fcitx5-android/fcitx5-android)。

下载：[Releases](https://github.com/liu-xindi/fcitx5-android/releases)。问题在 [Issues](https://github.com/liu-xindi/fcitx5-android/issues) 反馈。
