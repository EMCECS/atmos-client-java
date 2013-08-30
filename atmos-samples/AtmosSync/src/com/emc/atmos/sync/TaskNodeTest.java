// Copyright (c) 2012, EMC Corporation.
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
//     + Redistributions of source code must retain the above copyright notice,
//       this list of conditions and the following disclaimer.
//     + Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     + The name of EMC Corporation may not be used to endorse or promote
//       products derived from this software without specific prior written
//       permission.
//
//      THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//      "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
//      TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//      PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
//      BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
//      CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
//      SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
//      INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
//      CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
//      ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
//      POSSIBILITY OF SUCH DAMAGE.
package com.emc.atmos.sync;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

public class TaskNodeTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleDirectedGraph<TaskNode, DefaultEdge> graph = new SimpleDirectedGraph<TaskNode, DefaultEdge>(DefaultEdge.class);

		SimpleTaskNode dir1 = new SimpleTaskNode("mkdir /root");
		dir1.addToGraph(graph);
		
		SimpleTaskNode dir2 = new SimpleTaskNode("mkdir /root/dir2" );
		dir2.addParent(dir1);
		dir2.addToGraph(graph);
		
		SimpleTaskNode file1 = new SimpleTaskNode("upload /root/dir2/file1.txt");
		file1.addParent(dir2);
		file1.addToGraph(graph);
		
		SimpleTaskNode file2 = new SimpleTaskNode("upload /root/dir2/file2.txt");
		file2.addParent(dir2);
		file2.addToGraph(graph);
		
		SimpleTaskNode dir3 = new SimpleTaskNode( "mkdir /root/dir3" );
		dir3.addParent(dir1);
		dir3.addToGraph(graph);
		
		// Floating node
		SimpleTaskNode dir4 = new SimpleTaskNode( "mkdir /root2" );
		dir4.addToGraph(graph);
		
		// Separate tree
		SimpleTaskNode dir5 = new SimpleTaskNode("mkdir /root3" );
		dir5.addToGraph(graph);
		
		SimpleTaskNode file3 = new SimpleTaskNode("upload /root3/file3.txt");
		file3.addParent(dir5);
		file3.addToGraph(graph);
		
		LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
		ThreadPoolExecutor pool = new ThreadPoolExecutor(8, 8, 15, TimeUnit.SECONDS, queue);
		
		while(true) {
			synchronized (graph) {
				if( graph.vertexSet().size() == 0 ) {
					// We're done
					pool.shutdownNow();
					break;
				}
				
				// Look for available unsubmitted tasks
				BreadthFirstIterator<TaskNode, DefaultEdge> i = new BreadthFirstIterator<TaskNode, DefaultEdge>(graph);
				while( i.hasNext() ) {
					TaskNode t = i.next();
					if( graph.inDegreeOf(t) == 0 && !t.isQueued() ) {
						t.setQueued(true);
						System.out.println( "Submitting " + t );
						pool.submit(t);
					}
				}
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// Ignore
			}
		}
		
		System.out.println( "Complete" );
		System.exit(0);

	}

}
