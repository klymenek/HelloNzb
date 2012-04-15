/*******************************************************************************
 * HelloNzb -- The Binary Usenet Tool
 * Copyright (C) 2010-2011 Matthias F. Brandstetter
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package at.lame.hellonzb.renderer;

import java.awt.Component;
import java.awt.Font;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * A default cell renderer for a JTableHeader.
 * <P>
 * Extends {@link DefaultTableCellRenderer}.
 * <P>
 * DefaultTableHeaderCellRenderer attempts to provide identical behavior to the
 * renderer which the Swing subsystem uses by default, the Sun proprietary
 * class sun.swing.table.DefaultTableCellHeaderRenderer.
 * <P>
 * To apply any desired customization, DefaultTableHeaderCellRenderer may be
 * suitably extended.
 * 
 * @author http://tips4java.wordpress.com/2009/02/27/default-table-header-cell-renderer/
 */
public class AlignedTableHeaderRenderer extends DefaultTableCellRenderer
{
   /**
    * Constructs a <code>DefaultTableHeaderCellRenderer</code>.
    * <P>
    * The horizontal alignment and text position are set as appropriate to a
    * table header cell, and the renderer is set to be non-opaque.
    */
   public AlignedTableHeaderRenderer(int align) 
   {
      setHorizontalAlignment(align);
      setHorizontalTextPosition(align);
      setOpaque(false);
   }

   /**
    * Returns the default table header cell renderer.
    * <P>
    * The icon is set as appropriate for the header cell of a sorted or
    * unsorted column, and the border appropriate to a table header cell is
    * applied.
    * <P>
    * Subclasses may overide this method to provide custom content or
    * formatting.
    * 
    * @param table the <code>JTable</code>.
    * @param value the value to assign to the header cell
    * @param isSelected This parameter is ignored.
    * @param hasFocus This parameter is ignored.
    * @param row This parameter is ignored.
    * @param column the column of the header cell to render
    * @return the default table header cell renderer
    * 
    * @see DefaultTableCellRenderer#getTableCellRendererComponent(JTable,
    * Object, boolean, boolean, int, int)
    */
   @Override
   public Component getTableCellRendererComponent(JTable table, Object value,
         boolean isSelected, boolean hasFocus, int row, int column) 
   {
      super.getTableCellRendererComponent(table, value,
            isSelected, hasFocus, row, column);
      setIcon(getIcon(table, column));
      setBorder(UIManager.getBorder("TableHeader.cellBorder"));
   	  setFont(getFont().deriveFont(Font.BOLD));
      
      return this;
   }

   /**
    * Overloaded to return an icon suitable to a sorted column, or null if
    * the column is unsorted.
    * 
    * @param table the <code>JTable</code>.
    * @param column the colummn index.
    * @return the sort icon, or null if the column is unsorted.
    */
   protected Icon getIcon(JTable table, int column) 
   {
      SortKey sortKey = getSortKey(table, column);
      if (sortKey != null && sortKey.getColumn() == column) {
         SortOrder sortOrder = sortKey.getSortOrder();
         switch (sortOrder) {
            case ASCENDING:
               return UIManager.getIcon("Table.ascendingSortIcon");
            case DESCENDING:
               return UIManager.getIcon("Table.descendingSortIcon");
         }
      }
      return null;
   }

   protected SortKey getSortKey(JTable table, int column) 
   {
      RowSorter<?> rowSorter = table.getRowSorter();
      if(rowSorter == null) {
         return null;
      }

      List<?> sortedColumns = rowSorter.getSortKeys();
      if(sortedColumns.size() > 0) {
         return (SortKey) sortedColumns.get(0);
      }
      return null;
   }
}





























