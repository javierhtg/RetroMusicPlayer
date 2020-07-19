/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package code.name.monkey.retromusic.preferences

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_IN
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import code.name.monkey.appthemehelper.common.prefs.supportv7.ATEDialogPreference
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.dialogs.BlacklistFolderChooserDialog
import code.name.monkey.retromusic.extensions.colorButtons
import code.name.monkey.retromusic.extensions.colorControlNormal
import code.name.monkey.retromusic.providers.BlacklistStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.util.*

class BlacklistPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
    defStyleRes: Int = -1
) : ATEDialogPreference(context, attrs, defStyleAttr, defStyleRes) {

    init {
        icon?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                context.colorControlNormal(),
                SRC_IN
            )
    }
}

class BlacklistPreferenceDialog : DialogFragment(), BlacklistFolderChooserDialog.FolderCallback {
    companion object {
        fun newInstance(): BlacklistPreferenceDialog {
            return BlacklistPreferenceDialog()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val chooserDialog =
            childFragmentManager.findFragmentByTag("FOLDER_CHOOSER") as BlacklistFolderChooserDialog?
        chooserDialog?.setCallback(this)
        refreshBlacklistData()
        return MaterialAlertDialogBuilder(
            requireActivity(),
            R.style.ThemeOverlay_MaterialComponents_Dialog_Alert
        )
            .setTitle(R.string.blacklist)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                dismiss()
            }
            .setNeutralButton(R.string.clear_action) { _, _ ->
                MaterialAlertDialogBuilder(
                    requireActivity(),
                    R.style.ThemeOverlay_MaterialComponents_Dialog_Alert
                )
                    .setTitle(R.string.clear_blacklist)
                    .setMessage(R.string.do_you_want_to_clear_the_blacklist)
                    .setPositiveButton(R.string.clear_action) { _, _ ->
                        BlacklistStore.getInstance(
                            requireContext()
                        ).clear()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create()
                    .colorButtons()
                    .show()
            }
            .setNegativeButton(R.string.add_action) { _, _ ->
                val dialog = BlacklistFolderChooserDialog.create()
                dialog.setCallback(this@BlacklistPreferenceDialog)
                dialog.show(requireActivity().supportFragmentManager, "FOLDER_CHOOSER")
            }
            .setItems(paths.toTypedArray()) { _, which ->
                MaterialAlertDialogBuilder(
                    requireActivity(),
                    R.style.ThemeOverlay_MaterialComponents_Dialog_Alert
                ).setTitle(R.string.remove_from_blacklist)
                    .setMessage(
                        HtmlCompat.fromHtml(
                            String.format(
                                getString(
                                    R.string.do_you_want_to_remove_from_the_blacklist
                                ),
                                paths[which]
                            ),
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )
                    .setPositiveButton(R.string.remove_action) { _, _ ->
                        BlacklistStore.getInstance(requireContext())
                            .removePath(File(paths[which]))
                        refreshBlacklistData()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show().colorButtons()
            }
            .create().colorButtons()
    }

    private lateinit var paths: ArrayList<String>

    private fun refreshBlacklistData() {
        this.paths = BlacklistStore.getInstance(requireContext()).paths
        val dialog = dialog as MaterialAlertDialogBuilder?
        dialog?.setItems(paths.toTypedArray(), null)
    }

    override fun onFolderSelection(dialog: BlacklistFolderChooserDialog, folder: File) {
        BlacklistStore.getInstance(requireContext()).addPath(folder)
        refreshBlacklistData()
    }
}
