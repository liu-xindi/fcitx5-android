/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2021-2026 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.annotation.Keep
import androidx.core.view.allViews
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.core.InputMethodEntry
import org.fcitx.fcitx5.android.core.KeyState
import org.fcitx.fcitx5.android.core.KeyStates
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.data.prefs.ManagedPreference
import org.fcitx.fcitx5.android.data.theme.Theme
import org.fcitx.fcitx5.android.input.popup.PopupAction
import splitties.views.imageResource

@SuppressLint("ViewConstructor")
class TextKeyboard(
    context: Context,
    theme: Theme
) : BaseKeyboard(context, theme, Layout) {

    enum class CapsState { None, Once, Lock }

    companion object {
        const val Name = "Text"

        private val SelectionKey = AlphabetKey(
            "S", "*", punctuationText = "∗", oppositeAction = KeyAction.EditingAction(KeyAction.EditingAction.Op.ToggleSelection)
        )

        private val CtrlKey = LayoutSwitchKey("?123", swipeAction = KeyAction.CtrlAction)

        val Layout: List<List<KeyDef>> = listOf(
            listOf(
                AlphabetSymbolDigitKey("Q", "[", "1"),
                AlphabetSymbolDigitKey("W", "]", "2"),
                AlphabetSymbolDigitKey("E", "{", "3"),
                AlphabetSymbolDigitKey("R", "}", "4"),
                AlphabetSymbolDigitKey("T", "<", "5"),
                AlphabetSymbolDigitKey("Y", ">", "6"),
                AlphabetSymbolDigitKey("U", "|", "7"),
                AlphabetSymbolDigitKey("I", "&", "8"),
                AlphabetSymbolDigitKey("O", ";", "9"),
                AlphabetSymbolDigitKey("P", "_", "0")
            ),
            listOf(
                AlphabetKey("A", "@", oppositeAction = KeyAction.EditingAction(KeyAction.EditingAction.Op.SelectAll)),
                SelectionKey,
                AlphabetKey("D", "+", oppositeAction = KeyAction.FcitxKeyAction("^"), oppositeText = "^"),
                AlphabetKey("F", "-"),
                AlphabetKey("G", "=", oppositeAction = KeyAction.FcitxKeyAction("%"), oppositeText = "%"),
                AlphabetKey("H", "/", oppositeAction = KeyAction.EditingAction(KeyAction.EditingAction.Op.Left), oppositeText = "←"),
                AlphabetKey("J", "#", oppositeAction = KeyAction.EditingAction(KeyAction.EditingAction.Op.Down), oppositeText = "↓"),
                AlphabetKey("K", "(", oppositeAction = KeyAction.EditingAction(KeyAction.EditingAction.Op.Up), oppositeText = "↑"),
                AlphabetKey("L", ")", oppositeAction = KeyAction.EditingAction(KeyAction.EditingAction.Op.Right), oppositeText = "→")
            ),
            listOf(
                CapsKey(),
                AlphabetKey("Z", "'", oppositeAction = KeyAction.FcitxKeyAction("`"), oppositeText = "`"),
                AlphabetKey("X", ":", oppositeAction = KeyAction.EditingAction(KeyAction.EditingAction.Op.Cut)),
                AlphabetKey("C", "\"", oppositeAction = KeyAction.EditingAction(KeyAction.EditingAction.Op.Copy)),
                AlphabetKey("V", "?", oppositeAction = KeyAction.EditingAction(KeyAction.EditingAction.Op.Paste)),
                AlphabetKey("B", "!", oppositeAction = KeyAction.EditingAction(KeyAction.EditingAction.Op.Home), oppositeText = "|<"),
                AlphabetKey("N", "$", oppositeAction = KeyAction.EditingAction(KeyAction.EditingAction.Op.End), oppositeText = ">|"),
                AlphabetKey("M", "\\"),
                BackspaceKey()
            ),
            listOf(
                CtrlKey,
                CommaKey(0.1f, KeyDef.Appearance.Variant.Alternative),
                LanguageKey(),
                SpaceKey(),
                AltSymbolKey(".", "~", 0.1f, KeyDef.Appearance.Variant.Alternative),
                ReturnKey()
            )
        )
    }

    val caps: ImageKeyView by lazy { findViewById(R.id.button_caps) }
    val backspace: ImageKeyView by lazy { findViewById(R.id.button_backspace) }
    val quickphrase: ImageKeyView by lazy { findViewById(R.id.button_quickphrase) }
    val lang: ImageKeyView by lazy { findViewById(R.id.button_lang) }
    val space: TextKeyView by lazy { findViewById(R.id.button_space) }
    val `return`: ImageKeyView by lazy { findViewById(R.id.button_return) }

    private val showLangSwitchKey = AppPrefs.getInstance().keyboard.showLangSwitchKey

    @Keep
    private val showLangSwitchKeyListener = ManagedPreference.OnChangeListener<Boolean> { _, v ->
        updateLangSwitchKey(v)
    }

    private val keepLettersUppercase by AppPrefs.getInstance().keyboard.keepLettersUppercase

    init {
        updateLangSwitchKey(showLangSwitchKey.getValue())
        showLangSwitchKey.registerOnChangeListener(showLangSwitchKeyListener)
    }

    private val textKeys: List<TextKeyView> by lazy {
        allViews.filterIsInstance(TextKeyView::class.java).toList()
    }

    private val selectionKey: TextKeyView by lazy {
        textKeys.first { it.def === SelectionKey.appearance }
    }

    fun updateSelectionKey(active: Boolean) {
        selectionKey.setHighlighted(active)
    }

    private val ctrlKey: TextKeyView by lazy {
        textKeys.first { it.def === CtrlKey.appearance }
    }

    fun updateCtrlKey(active: Boolean) {
        ctrlKey.setHighlighted(active)
    }

    private var capsState: CapsState = CapsState.None

    private fun transformAlphabet(c: String): String {
        return when (capsState) {
            CapsState.None -> c.lowercase()
            else -> c.uppercase()
        }
    }

    private var punctuationMapping: Map<String, String> = mapOf()
    private fun transformPunctuation(p: String) = punctuationMapping.getOrDefault(p, p)

    override fun onAction(action: KeyAction, source: KeyActionListener.Source) {
        var transformed = action
        when (action) {
            is KeyAction.FcitxKeyAction -> when (source) {
                KeyActionListener.Source.Keyboard -> {
                    when (capsState) {
                        CapsState.None -> {
                            transformed = action.copy(act = action.act.lowercase())
                        }
                        CapsState.Once -> {
                            transformed = action.copy(
                                act = action.act.uppercase(),
                                states = KeyStates(KeyState.Virtual, KeyState.Shift)
                            )
                            switchCapsState()
                        }
                        CapsState.Lock -> {
                            transformed = action.copy(
                                act = action.act.uppercase(),
                                states = KeyStates(KeyState.Virtual, KeyState.CapsLock)
                            )
                        }
                    }
                }
                KeyActionListener.Source.Popup -> {
                    if (capsState == CapsState.Once) {
                        switchCapsState()
                    }
                }
            }
            is KeyAction.CapsAction -> switchCapsState(action.lock)
            else -> {}
        }
        super.onAction(transformed, source)
    }

    override fun onAttach() {
        capsState = CapsState.None
        updateCapsButtonIcon()
        updateAlphabetKeys()
    }

    override fun onReturnDrawableUpdate(returnDrawable: Int) {
        `return`.img.imageResource = returnDrawable
    }

    override fun onPunctuationUpdate(mapping: Map<String, String>) {
        punctuationMapping = mapping
        updatePunctuationKeys()
    }

    override fun onInputMethodUpdate(ime: InputMethodEntry) {
        space.mainText.text = buildString {
            append(ime.displayName)
            ime.subMode.run { label.ifEmpty { name.ifEmpty { null } } }?.let { append(" ($it)") }
        }
        if (capsState != CapsState.None) {
            switchCapsState()
        }
    }

    private fun transformPopupPreview(c: String): String {
        if (c.length != 1) return c
        if (c[0].isLetter()) return transformAlphabet(c)
        return transformPunctuation(c)
    }

    override fun onPopupAction(action: PopupAction) {
        val newAction = when (action) {
            is PopupAction.PreviewAction -> action.copy(content = transformPopupPreview(action.content))
            is PopupAction.PreviewUpdateAction -> action.copy(content = transformPopupPreview(action.content))
            is PopupAction.ShowKeyboardAction -> {
                when (action.keyboard) {
                    is KeyDef.Popup.Keyboard.Preset -> {
                        val label = action.keyboard.label
                        if (label.length == 1 && label[0].isLetter())
                            action.copy(
                                keyboard = action.keyboard.copy(label = transformAlphabet(label))
                            )
                        else action
                    }
                    is KeyDef.Popup.Keyboard.Explicit -> action
                }
            }
            else -> action
        }
        super.onPopupAction(newAction)
    }

    private fun switchCapsState(lock: Boolean = false) {
        capsState =
            if (lock) {
                when (capsState) {
                    CapsState.Lock -> CapsState.None
                    else -> CapsState.Lock
                }
            } else {
                when (capsState) {
                    CapsState.None -> CapsState.Once
                    else -> CapsState.None
                }
            }
        updateCapsButtonIcon()
        updateAlphabetKeys()
    }

    private fun updateCapsButtonIcon() {
        caps.img.apply {
            imageResource = when (capsState) {
                CapsState.None -> R.drawable.ic_capslock_none
                CapsState.Once -> R.drawable.ic_capslock_once
                CapsState.Lock -> R.drawable.ic_capslock_lock
            }
        }
    }

    private fun updateLangSwitchKey(visible: Boolean) {
        lang.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun updateAlphabetKeys() {
        textKeys.forEach {
            if (it.def !is KeyDef.Appearance.AltText) return
            it.mainText.text = it.def.displayText.let { str ->
                if (str.length != 1 || !str[0].isLetter()) return@forEach
                if (keepLettersUppercase) str.uppercase() else transformAlphabet(str)
            }
        }
    }

    private fun updatePunctuationKeys() {
        textKeys.forEach {
            if (it is AltTextKeyView) {
                it.def as KeyDef.Appearance.AltText
                it.altText.text = transformPunctuation(it.def.altText)
                it.def.oppositeAltText?.let { text -> it.oppositeAltText.text = transformPunctuation(text) }
                if (!it.def.displayText[0].isLetter()) {
                    it.mainText.text = transformPunctuation(it.def.displayText)
                }
            } else {
                it.def as KeyDef.Appearance.Text
                it.mainText.text = it.def.displayText.let { str ->
                    if (str[0].run { isLetter() || isWhitespace() }) return@forEach
                    transformPunctuation(str)
                }
            }
        }
    }

}